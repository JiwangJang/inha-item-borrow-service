"use client";

import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import { REQUEST_STATE_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import { notFound, useRouter } from "next/navigation";
import { useContext } from "react";
import ItemBorrowConditions from "../../borrower-request/ItemBorrowConditions";

export default function SingleBorrowRequestPage({ requestId }: { requestId: string }) {
    const router = useRouter();
    const requestList = useContext(BorrowRequestContext).requestList;
    const currentRequest = requestList.find((r) => String(r.id) == requestId);
    if (currentRequest == undefined) {
        notFound();
    }

    const { id, item, borrowAt, returnAt, state, manager } = currentRequest;
    const { name: itemName, location: itemLocation, password: itemPassword, price: itemPrice } = item;

    let borrowState;

    switch (state) {
        case REQUEST_STATE_TYPE.ASSIGNED:
            borrowState = `검토중(담당자 : ${manager!.name})`;
            break;
        case REQUEST_STATE_TYPE.PENDING:
            borrowState = "검토중(담당자 배정안됨)";
            break;
        case REQUEST_STATE_TYPE.PERMIT:
            borrowState = "허가승인";
            break;
        default:
            borrowState = "불허가";
            break;
    }

    return (
        <div className="mt-5">
            <p className="black-20px mb-3">✅ 기본정보</p>
            <InfoTable>
                <InfoRow label="대여번호" value={String(id)} />
                <InfoRow label="대여물품" value={itemName} />
                <InfoRow label="대여일시" value={dateFormatter(borrowAt)} />
                <InfoRow label="반납일시" value={`${dateFormatter(returnAt)}(예정)`} />
                <InfoRow label="대여상태" value={borrowState} />
                <InfoRow label="보관위치" value={itemLocation ?? "-"} />
                <InfoRow label="비밀번호" value={itemPassword ?? "-"} />
            </InfoTable>

            {state != REQUEST_STATE_TYPE.PERMIT ? (
                // 허가가 아님
                state == REQUEST_STATE_TYPE.REJECT ? (
                    // 불허가면
                    <p className="mt-2">거절사유 : {currentRequest.response!.rejectReason}</p>
                ) : (
                    // 불허가가 아니면(PENDING or ASSIGNED)
                    <p className="mt-2">보관위치와 비밀번호는 허가 승인후 나타납니다.</p>
                )
            ) : null}

            <div className="flex gap-2 pt-2">
                <Button
                    title="대여신청서 확인"
                    className="py-3 flex-1 regular-16px bg-white! text-black! border border-black!"
                    onClick={() => router.push(`/borrow-list/${id}/paper`)}
                />
                <Button
                    title="반납신청하기"
                    disabled={state != REQUEST_STATE_TYPE.PERMIT}
                    className="py-3 flex-1 bold-16px"
                />
            </div>
            <div className="mt-5">
                <p className="black-20px mb-2">📝물품대여조건</p>
                <ItemBorrowConditions price={itemPrice} />
            </div>
        </div>
    );
}
