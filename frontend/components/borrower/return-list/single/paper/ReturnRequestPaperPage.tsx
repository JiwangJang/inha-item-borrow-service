"use client";

import API_SERVER from "@/apiServer";
import LoginRequired from "@/components/borrower/LoginRequired";
import Button from "@/components/utilities/Button";
import ReturnDateSelector, { toKstOffsetDateTimeString } from "@/components/utilities/ReturnDateSelector";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import BorrowerContext from "@/context/BorrowerContext";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import ItemContext from "@/context/ItemContext";
import { REQUEST_STATE_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import errorHandler from "@/utilities/errorHandler";
import axios from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function ReturnRequestPaperPage({ requestId }: { requestId: string }) {
    const [returnDateSelector, setReturnDateSelector] = useState(false);
    const [loading, setLoading] = useState(false);

    const { requestList, setRequestList } = useContext(BorrowRequestContext);
    const { borrowerInfo } = useContext(BorrowerContext);
    const current = requestList.find((r) => String(r.id) == requestId);

    const [confirmModal, setConfirmModal] = useState(false);

    if (current == null) {
        notFound();
    }

    if (borrowerInfo == null) {
        return <LoginRequired />;
    }

    const { name: stName, phoneNumber, id: studentNumber } = borrowerInfo;
    const { borrowAt, returnAt, createdAt, state } = current;
    const { name: itemName } = current.item;
    const response = current.response;
    const responseAt = response?.createdAt;

    const returnRequestRevise = async (returnAtString: string) => {
        // borrowAt: `${borrowDate}T${borrowTime}:00+09:00`, 이런식으로 하면 됨

        if (setRequestList == null) {
            alert("새로고침후 다시 시도해주세요. 지속적으로 발생할 경우 관리자에게 연락해주세요.");
            return;
        }

        if (new Date() > new Date(returnAtString)) {
            // 반납일시가 대여 일시보다 이전이거나, 현재보다 이전인 경우 요청 못보냄
            alert("올바른 반납일시를 선택해주세요.");
            return;
        }
        setLoading(true);
        try {
            const body = {
                returnAt: returnAtString,
                borrowAt: toKstOffsetDateTimeString(Number(borrowAt)),
            };

            await axios.patch(`${API_SERVER}/requests/${requestId}/patch`, body, {
                withCredentials: true,
            });

            setRequestList(
                requestList.map((rq) => {
                    if (String(rq.id) == requestId) {
                        return {
                            ...rq,
                            borrowAt: body.borrowAt,
                            returnAt: body.returnAt,
                        };
                    }
                    return rq;
                }),
            );

            alert("반납요청 수정을 완료했습니다.");
            setReturnDateSelector(false);
        } catch (error) {
            if (axios.isAxiosError(error)) {
                errorHandler(error);
                return;
            }
            alert(
                "알 수 없는 에러가 발생했습니다. 다시 한 번 시도해보시고, 지속적으로 발생할 경우 관리자에게 연락해주세요.",
            );
            console.error(error);
        }
        setLoading(false);
    };

    return (
        <div className="bg-white border border-boxBorder rounded-xl mt-5 py-5 px-6">
            <p className="black-24px text-center">물품반납확인서</p>
            <div className="mt-5 regular-16px flex flex-col gap-1">
                <SameSpaceRow label="이름" value={stName} />
                <SameSpaceRow label="연락처" value={phoneNumber} />
                <SameSpaceRow label="학번" value={studentNumber} />
                <SameSpaceRow label="요청번호" value={requestId} />
                <SameSpaceRow label="대여물품" value={itemName} />
                <SameSpaceRow label="대여일시" value={dateFormatter(borrowAt)} />
                <SameSpaceRow label="반납일시" value={dateFormatter(returnAt)} />
            </div>

            <div className="mt-4 text-center">
                <p className="bold-16px">본인은 위와 같이 물품반납신청합니다.</p>
                <p className="regular-16px">{dateFormatter(createdAt).slice(0, 13)}</p>
            </div>

            {state == REQUEST_STATE_TYPE.PERMIT || state == REQUEST_STATE_TYPE.REJECT ? (
                <div className="mt-4 pt-4 border-t border-black text-center bold-16px flex flex-col justify-center items-center">
                    <p>위 사람의 물품반납신청을 {state == REQUEST_STATE_TYPE.PERMIT ? "허가" : "불허가"}합니다.</p>
                    <div className="my-5 relative w-fit">
                        <p className="black-20px">미래융합대학 학생회장</p>
                        <div
                            className="-top-2 -right-3 absolute w-10 h-10"
                            style={{
                                backgroundImage: "url(/images/stamp.png)",
                                backgroundSize: "cover",
                                backgroundRepeat: "no-repeat",
                            }}
                        ></div>
                    </div>
                    <p>{dateFormatter(responseAt!).slice(0, 13)}</p>
                    {state == REQUEST_STATE_TYPE.PERMIT ? null : <p>불허가 사유 : {response?.rejectReason}</p>}
                </div>
            ) : state == REQUEST_STATE_TYPE.PENDING ? (
                <Button
                    title="반납일시 수정하기"
                    className="w-full py-2 mt-3 bold-16px"
                    onClick={() => setReturnDateSelector(true)}
                />
            ) : (
                <p className="text-center mt-4">담당자 배정 이전에만 수정 또는 취소가 가능합니다.</p>
            )}

            <ReturnDateSelector
                initialReturnAt={dateFormatter(returnAt)}
                open={returnDateSelector}
                onClose={() => setReturnDateSelector(false)}
                sendFunc={returnRequestRevise}
            />
        </div>
    );
}
