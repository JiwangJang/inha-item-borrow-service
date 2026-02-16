"use client";

import API_SERVER from "@/apiServer";
import LoginRequired from "@/components/borrower/LoginRequired";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import PickerField from "@/components/utilities/select/PickerField";
import Select from "@/components/utilities/select/Select";
import BorrowerContext from "@/context/BorrowerContext";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import ItemContext from "@/context/ItemContext";
import { REQUEST_STATE_TYPE } from "@/types/RequestInterface";
import { dateFormatter, dateFormatterForDateInput } from "@/utilities/dateFormatter";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function BorrowRequestRevisePage({ id }: { id: string }) {
    const router = useRouter();
    const { requestList, setRequestList } = useContext(BorrowRequestContext);
    const { itemList } = useContext(ItemContext);
    const { borrowerInfo } = useContext(BorrowerContext);

    if (borrowerInfo == null) {
        return <LoginRequired />;
    }

    const currentRequest = requestList.find((r) => String(r.id) == id);

    if (currentRequest == null) {
        notFound();
    }

    if (currentRequest.cancel) {
        alert("취소된 요청은 수정할 수 없습니다.");
        router.back();
        return;
    }

    if (currentRequest.state != REQUEST_STATE_TYPE.PENDING) {
        alert("관리자 배정 이전에만 수정이 가능합니다.");
        router.back();
        return;
    }

    const currentItem = itemList.find((it) => it.id == currentRequest.item.id)!;

    const initialBorrowDate = dateFormatterForDateInput(currentRequest.borrowAt).slice(0, 10);
    const initialBorrowTime = dateFormatterForDateInput(currentRequest.borrowAt).slice(11, 16);
    const initialReturnDate = dateFormatterForDateInput(currentRequest.returnAt).slice(0, 10);
    const initialReturnTime = dateFormatterForDateInput(currentRequest.returnAt).slice(11, 16);

    const [borrowDate, setBorrowDate] = useState<string>(initialBorrowDate);
    const [borrowTime, setBorrowTime] = useState<string>(initialBorrowTime);
    const [returnDate, setReturnDate] = useState<string>(initialReturnDate);
    const [returnTime, setReturnTime] = useState<string>(initialReturnTime);

    const [confirmModal, setConfirmModal] = useState(false);

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

    const buttonOn = borrowDate != "" && borrowTime != "" && returnDate != "" && returnTime != "";
    const today = new Date();

    const revise = async () => {
        try {
            const body = {
                borrowAt: `${borrowDate}T${borrowTime}:00+09:00`,
                returnAt: `${returnDate}T${returnTime}:00+09:00`,
            };

            await axios.patch(`${API_SERVER}/requests/${id}/patch`, body, { withCredentials: true });

            if (setRequestList) {
                // 상태 변경 반영
                setRequestList(
                    requestList.map((rq) => {
                        if (String(rq.id) == id) {
                            return {
                                ...rq,
                                borrowAt: `${borrowDate}T${borrowTime}:00+09:00`,
                                returnAt: `${returnDate}T${returnTime}:00+09:00`,
                            };
                        }
                        return rq;
                    }),
                );
            }

            alert("수정이 완료됐습니다.");
            router.back();
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            } else {
                alert("알 수 없는 에러입니다. 새로고침 후 다시시도해보시고, 지속되면 관리자에게 연락해주세요.");
            }
        }
    };

    return (
        <div className="mt-5">
            <div>
                <p className="bold-18px mb-2">1. 대여물품을 선택해주세요</p>
                <Select
                    value={currentItem.name}
                    options={[]}
                    onChange={() => {}}
                    placeholder="대여물품 선택"
                    disabled={true}
                />
                <p className="regular-14px text-placeholder mt-2">대여물품은 변경할 수 없습니다.</p>
            </div>

            <div className="mt-3">
                <p className="bold-18px mb-2">2. 대여일시를 선택해주세요</p>
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
                <p className="bold-18px mb-2">3. 예상 반납일시를 선택해주세요</p>
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
                title="대여신청 수정"
                className="w-full p-3 bold-18px mt-6"
                disabled={!buttonOn}
                onClick={() => setConfirmModal(true)}
            />
            <ConfirmModal
                open={confirmModal}
                message="위와 같이 수정하시겠어요?"
                onClose={() => setConfirmModal(false)}
                onConfirm={revise}
                title="알림"
            />
        </div>
    );
}
