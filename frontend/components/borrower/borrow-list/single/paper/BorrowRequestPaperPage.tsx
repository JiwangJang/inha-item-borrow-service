"use client";

import API_SERVER from "@/apiServer";
import LoginRequired from "@/components/borrower/LoginRequired";
import Button from "@/components/utilities/Button";
import ConfirmModal from "@/components/utilities/modal/ConfirmModal";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import BorrowerContext from "@/context/BorrowerContext";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import ItemContext from "@/context/ItemContext";
import { ITEM_STATE_TYPE } from "@/types/ItemStateType";
import { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import axios from "axios";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function BorrowRequestPaperPage({ requestId }: { requestId: string }) {
    const { requestList } = useContext(BorrowRequestContext);
    const setRequestList = useContext(BorrowRequestContext).setRequestList;
    const { itemList, setItemList } = useContext(ItemContext);
    const borrowerInfo = useContext(BorrowerContext).borrowerInfo;
    const current = requestList.filter((rq) => rq.type == REQUEST_TYPE.BORROW).find((r) => String(r.id) == requestId);

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
    const router = useRouter();

    const cancelRequest = async () => {
        if (current.state != REQUEST_STATE_TYPE.PENDING) {
            alert("관리자 배정 이전에만 취소가 가능합니다.");
            return;
        }

        try {
            await axios.patch(`${API_SERVER}/requests/${requestId}/cancel`, null, { withCredentials: true });

            if (setRequestList) {
                // 취소한 요청 표시
                setRequestList(
                    requestList.map((rq) => {
                        if (String(rq.id) == requestId) {
                            return {
                                ...rq,
                                cancel: true,
                            };
                        }
                        return rq;
                    }),
                );
            }

            if (setItemList) {
                // 취소한 아이템 대여 가능하다는 상태로 보여지게 설정
                setItemList(
                    itemList.map((it) => {
                        if (it.id == current.item.id) {
                            return {
                                ...it,
                                state: ITEM_STATE_TYPE.AFFORD,
                            };
                        }
                        return it;
                    }),
                );
            }

            alert("해당 요청 취소가 완료됐습니다.");
            router.back();
        } catch (error) {
            console.error(error);
        }
    };

    const goRevisePage = () => {
        if (current.cancel) {
            alert("취소된 요청은 수정할 수 없습니다.");
            return;
        }
        if (current.state != REQUEST_STATE_TYPE.PENDING) {
            alert("관리자 배정 이전에만 수정이 가능합니다.");
            return;
        }
        router.push(`/borrow-list/${requestId}/revise`);
    };

    return (
        <div className="bg-white border border-boxBorder rounded-xl mt-5 py-5 px-6">
            <p className="black-24px text-center">물품대여신청서</p>
            <div className="mt-5 regular-16px flex flex-col gap-1">
                <SameSpaceRow label="이름" value={stName} />
                <SameSpaceRow label="연락처" value={phoneNumber} />
                <SameSpaceRow label="학번" value={studentNumber} />
                <SameSpaceRow label="요청번호" value={requestId} />
                <SameSpaceRow label="대여물품" value={itemName} />
                <SameSpaceRow label="대여일시" value={dateFormatter(borrowAt)} />
                <SameSpaceRow label="반납일시" value={`${dateFormatter(returnAt)}(예정)`} />
            </div>

            <div className="mt-4 text-center">
                <p className="bold-16px">본인은 위와 같이 물품대여신청합니다.</p>
                <p className="regular-16px">{dateFormatter(createdAt).slice(0, 13)}</p>
                {current.cancel ? <p>취소된 요청입니다.</p> : null}
            </div>

            {state == REQUEST_STATE_TYPE.PERMIT || state == REQUEST_STATE_TYPE.REJECT ? (
                <div className="mt-4 pt-4 border-t border-black text-center bold-16px flex flex-col justify-center items-center">
                    <p>위 사람의 물품대여신청을 {state == REQUEST_STATE_TYPE.PERMIT ? "허가" : "불허가"}합니다.</p>
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
                <div className="flex gap-1 mt-3">
                    <Button
                        title="취소하기"
                        className="w-full py-2 bg-white! border-2 border-black text-black!"
                        onClick={() => setConfirmModal(true)}
                    />
                    <Button title="수정하기" className="w-full py-2" onClick={goRevisePage} />
                </div>
            ) : (
                <p className="text-center mt-4">담당자 배정 이전에만 수정 또는 취소가 가능합니다.</p>
            )}

            <ConfirmModal
                title="알림"
                message="정말 취소하시겠습니까?"
                onClose={() => setConfirmModal(false)}
                onConfirm={cancelRequest}
                open={confirmModal}
            />
        </div>
    );
}
