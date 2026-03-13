"use client";

import LoginRequired from "@/components/borrower/LoginRequired";
import SameSpaceRow from "@/components/utilities/SameSpaceRow";
import BorrowerContext from "@/context/BorrowerContext";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import { notFound } from "next/navigation";
import { useContext, useState } from "react";

export default function BorrowRequestPaperPage({ requestId }: { requestId: string }) {
    const { requestList } = useContext(BorrowRequestContext);
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
    const { borrowAt, returnAt, createdAt, viewPasswordAt, state } = current;
    const { name: itemName } = current.item;
    const response = current.response;
    const responseDate = response?.createdAt == null ? null : new Date(response.createdAt);
    const createdAtDate = new Date(createdAt);
    const viewPasswordAtDate = new Date(createdAt);

    return (
        <div>
            <p className="bold-20px mt-5">📃 물품대여신청서</p>
            <div className="bg-white border border-boxBorder rounded-xl mt-2 py-5 px-6">
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
                    <p className="regular-16px">{`${createdAtDate.getFullYear()}. ${createdAtDate.getMonth() + 1}. ${createdAtDate.getDate()}.`}</p>
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
                        <p>{`${responseDate!.getFullYear()}. ${responseDate!.getMonth() + 1}. ${responseDate!.getDate()}.`}</p>
                        {state == REQUEST_STATE_TYPE.PERMIT ? null : <p>불허가 사유 : {response?.rejectReason}</p>}
                    </div>
                ) : null}
            </div>

            {viewPasswordAt != null ? (
                <>
                    <p className="bold-20px mt-4">✅ 수령확인증</p>
                    <div className="bg-white border border-boxBorder rounded-xl mt-2 py-5 px-6">
                        <p className="black-24px text-center">물품수령확인증</p>
                        <div className="mt-5 regular-16px flex flex-col gap-1">
                            <SameSpaceRow label="이름" value={stName} />
                            <SameSpaceRow label="학번" value={studentNumber} />
                            <SameSpaceRow label="대여물품" value={itemName} />
                            <SameSpaceRow label="수령일시" value={dateFormatter(viewPasswordAt!)} />
                        </div>
                        <div className="mt-4 text-center">
                            <p className="bold-16px">본인은 위와 같이 물품을 수령하였습니다.</p>
                            <p className="regular-16px">{`${viewPasswordAtDate.getFullYear()}. ${viewPasswordAtDate.getMonth() + 1}. ${viewPasswordAtDate.getDate()}.`}</p>
                        </div>
                    </div>
                </>
            ) : null}
        </div>
    );
}
