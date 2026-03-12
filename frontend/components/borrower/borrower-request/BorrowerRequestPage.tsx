"use client";

import Select from "@/components/utilities/select/Select";
import { useContext, useEffect, useState } from "react";
import ItemBorrowConditions from "./ItemBorrowConditions";
import ItemContext from "@/context/ItemContext";
import Image from "next/image";
import PickerField from "@/components/utilities/select/PickerField";
import Button from "@/components/utilities/Button";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import axios from "axios";
import API_SERVER from "@/apiServer";
import RequestInterface, { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import BorrowerContext from "@/context/BorrowerContext";
import LoginRequired from "../LoginRequired";
import { useRouter } from "next/navigation";
import AlertModal from "@/components/utilities/modal/AlertModal";

export default function BorrowerRequestPage() {
    const borrowerInfo = useContext(BorrowerContext).borrowerInfo;
    if (borrowerInfo == null) {
        return <LoginRequired />;
    }

    const { requestList, setRequestList } = useContext(BorrowRequestContext);
    const recentRequest = [...requestList].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    )[0];

    const [alertModal, setAlertModal] = useState(false);
    const [alertMsg, setAlertMsg] = useState("");
    const [confirmFunc, setConfirmFunc] = useState<() => void>(() => {});

    useEffect(() => {
        if (borrowerInfo.ban) {
            setAlertModal(true);
            setAlertMsg("이용금지인 경우 대여요청이 불가능합니다. 먼저 이용금지 사유를 해결해주세요.");
            setConfirmFunc(() => () => router.back());
            return;
        }
        if (borrowerInfo.agreementVersion == null) {
            setAlertModal(true);
            setAlertMsg(
                "개인정보 수집 이용에 동의하지 않은 경우 대여요청이 불가능합니다. 먼저 개인정보 수집에 동의해주세요.",
            );
            setConfirmFunc(() => () => router.push("/borrower-info/agreement/v1"));
            return;
        }
        if (!borrowerInfo.verify) {
            setAlertModal(true);
            setAlertMsg("학생회비 납부인증을 하셔야 이용가능합니다.");
            setConfirmFunc(() => () => router.push("/borrower-info/student-council-fee"));
            return;
        }
        if (
            recentRequest != null &&
            ((recentRequest.type == REQUEST_TYPE.BORROW && recentRequest.state != REQUEST_STATE_TYPE.REJECT) ||
                (recentRequest.type == REQUEST_TYPE.RETURN && recentRequest.state != REQUEST_STATE_TYPE.PERMIT))
        ) {
            // 최신 요청이 존재하면서
            // 최신 요청이 BORROW 타입이면서 거절(REJECT) 이외의 상태이거나
            // 최신 요청이 RETURN 타입이면서 승인(PERMIT) 이외의 상태라면,
            // 추가 대여요청이 불가능하다.
            setAlertModal(true);
            setAlertMsg("한 번에 한 물건만 빌릴 수 있습니다.");
            setConfirmFunc(() => () => router.back());
            return;
        }
    }, []);

    const { itemList, setItemList } = useContext(ItemContext);
    const router = useRouter();

    const [item, setItem] = useState<string>("");
    const [agree, setAgree] = useState<boolean>(false);
    const [borrowDate, setBorrowDate] = useState<string>("");
    const [borrowTime, setBorrowTime] = useState<string>("");
    const [returnDate, setReturnDate] = useState<string>("");
    const [returnTime, setReturnTime] = useState<string>("");
    // 사용자는 카테고리만 고르고 클라이언트 정보에서 빌릴 수 있는거 요청 넣고, 안되면 다른 아이디로 요청넣기

    const buttonOn =
        agree && borrowDate != "" && borrowTime != "" && returnDate != "" && returnTime != "" && item != "";
    const today = new Date();

    const checkboxImage =
        `${agree ? "/images/icons/others/active" : "/images/icons/others/inactive"}` + "/password.png";
    const itemNameList = Array.from(new Set((itemList ?? []).map((it) => it.name)));
    const itemNamePriceObj = (itemList ?? []).reduce(
        (acc, cur) => {
            if (acc[cur.name] == null) acc[cur.name] = cur.price;
            return acc;
        },
        {} as Record<string, number>,
    );

    const itemSelectOnChange = (value: string) => {
        setItem(value);
    };

    const checkBoxOnClickFunc = () => {
        setAgree(!agree);
    };

    const borrowDateFieldOnChange = (value: string) => {
        setBorrowDate(value);
    };

    const borrowTimeFieldOnChange = (value: string) => {
        setBorrowTime(value);
    };

    const returnDateFieldOnChange = (value: string) => {
        setReturnDate(value);
    };
    const returnTimeFieldOnChange = (value: string) => {
        setReturnTime(value);
    };

    const buttonOnclick = async () => {
        if (itemList.length == 0) return;
        const selectedItem = itemList.find((it) => it.state == ITEM_STATE_TYPE.AFFORD && it.name == item);
        if (selectedItem == null) {
            alert(`현재 빌릴 수 있는 ${item}이 없습니다.`);
            return;
        }
        if (!borrowerInfo.verify) {
            alert("학생회비 납부인증이 완료되지 않았습니다.");
            return;
        }
        try {
            const body = {
                itemId: selectedItem.id,
                borrowAt: `${borrowDate}T${borrowTime}:00+09:00`,
                returnAt: `${returnDate}T${returnTime}:00+09:00`,
                type: "BORROW",
            };

            const now = new Date();
            const minBorrowAt = new Date(now.getTime() + 60 * 60 * 1000); // now + 1 hour
            const borrowAtDate = new Date(body.borrowAt);
            const returnAtDate = new Date(body.returnAt);

            if (borrowAtDate < minBorrowAt) {
                // 대여일시가 현재시각보다 1시간 이전인 경우
                alert("대여일시는 현재 시각보다 1시간 이후여야합니다.");
                return;
            }

            if (borrowAtDate >= returnAtDate) {
                // 반납일시가 대여일시보다 늦은 경우
                alert("반납일시는 대여일시보다 이후여야합니다.");
                return;
            }

            const res = await axios.post(`${API_SERVER}/requests`, body, {
                withCredentials: true,
            });

            const result = res.data.data;

            const newRequest: RequestInterface = {
                prevRequestId: null,
                id: result.id,
                borrowAt: body.borrowAt,
                returnAt: body.returnAt,
                createdAt: result.createdAt,
                borrowerId: borrowerInfo.id,
                borrowerName: borrowerInfo.name,
                item: {
                    id: selectedItem.id,
                    name: selectedItem.name,
                    price: selectedItem.price,
                    location: null,
                    password: null,
                },
                cancel: false,
                manager: null,
                response: null,
                state: REQUEST_STATE_TYPE.PENDING,
                type: REQUEST_TYPE.BORROW,
            };

            if (setRequestList) {
                setRequestList(requestList.concat(newRequest));
            }

            if (setItemList) {
                setItemList(
                    itemList.map((it) => {
                        if (it.id == selectedItem.id) {
                            return {
                                ...it,
                                state: ITEM_STATE_TYPE.REVIEWING,
                            };
                        }

                        return it;
                    }),
                );
            }

            alert("대여신청이 완료됐습니다.");

            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                const errorObj = error.response?.data.data;
                const errorCode = errorObj?.errorCode;

                console.log(errorObj);

                if (errorCode === "INVALID_ITEM_ID") {
                    alert("누군가 해당 대여물품에 대해 먼저 대여신청을 했습니다. 다시 시도해주세요.");
                    if (setItemList) {
                        setItemList(
                            itemList.map((it) => {
                                if (it.id == selectedItem.id) {
                                    return {
                                        ...it,
                                        state: ITEM_STATE_TYPE.REVIEWING,
                                    };
                                }
                                return it;
                            }),
                        );
                    }
                    return;
                }

                // Any other Axios error
                alert(errorObj?.errorMessage ?? "요청 중 오류가 발생했습니다. 지속될 경우 관리자에게 연락해주세요.");
                return;
            }
            alert("요청 중 오류가 발생했습니다. 지속될 경우 관리자에게 연락해주세요.(x Axios)");
            console.error(error);
        }
    };

    return (
        <div className="mt-5">
            <div>
                <p className="bold-18px mb-2">1. 물품대여조건</p>
                <ItemBorrowConditions />
                <div className="flex mt-2">
                    <p className="flex-1 bold-16px">위 내용을 숙지하셨습니까?</p>
                    <div className="flex justify-end items-center cursor-pointer" onClick={checkBoxOnClickFunc}>
                        <span className="regular-16px mr-1 cursor-pointer pl-2">숙지했습니다.</span>
                        <Image src={checkboxImage} width={16} height={16} alt="체크박스" className="cursor-pointer" />
                    </div>
                </div>
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">2. 대여물품을 선택해주세요</p>
                <Select value={item} options={itemNameList} placeholder="대여물품 선택" onChange={itemSelectOnChange} />
                {item != "" ? (
                    <p className="mt-2 pl-0.5 bold-16px">
                        보증금 {Math.floor((itemNamePriceObj[item] * 0.3) / 1000) * 1000}원을 입금해주세요.
                    </p>
                ) : null}
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">3. 대여일시를 선택해주세요</p>
                <div className="flex gap-2">
                    <PickerField
                        type="date"
                        placeholder="대여일 선택"
                        min={`${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`}
                        onChange={borrowDateFieldOnChange}
                        value={borrowDate}
                    />
                    <PickerField
                        type="time"
                        placeholder="대여시간 선택"
                        onChange={borrowTimeFieldOnChange}
                        value={borrowTime}
                    />
                </div>
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">4. 예상 반납일시를 선택해주세요</p>
                <div className="mb-2 flex gap-2">
                    <PickerField
                        type="date"
                        placeholder="반납일 선택"
                        min={
                            borrowDate
                                ? borrowDate
                                : `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`
                        }
                        onChange={returnDateFieldOnChange}
                        value={returnDate}
                    />
                    <PickerField
                        type="time"
                        placeholder="반납시간 선택"
                        onChange={returnTimeFieldOnChange}
                        value={returnTime}
                    />
                </div>
                <p className="text-placeholder pl-0.5 leading-tight regular-14px">
                    대여 기간은 원칙적으로 대여한 날로부터 최대 7일로 하며, 공휴일 또는 학생회의 사정으로 대여 및 반납이
                    곤란한 경우, 대여자의 특별한 사정으로 학생회와 사전에 협의된 경우 합리적인 선에서 조정할 수 있음.
                </p>
            </div>

            <Button
                title="대여신청"
                className="w-full p-3 bold-18px mt-6"
                disabled={!buttonOn}
                onClick={buttonOnclick}
            />

            <AlertModal
                open={alertModal}
                message={alertMsg}
                onClose={() => setAlertModal(false)}
                onConfirm={confirmFunc}
                title="알림"
            />
        </div>
    );
}
