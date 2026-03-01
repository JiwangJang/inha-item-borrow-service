"use client";

import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import InfoRow from "@/components/utilities/InfoRow";
import InfoTable from "@/components/utilities/InfoTable";
import BorrowerContext from "@/context/BorrowerContext";
import BorrowRequestContext from "@/context/BorrowRequestContext";
import { REQUEST_STATE_TYPE, REQUEST_TYPE } from "@/types/RequestInterface";
import { dateFormatter } from "@/utilities/dateFormatter";
import { notFound, useRouter } from "next/navigation";
import { useContext, useState } from "react";

export default function SingleReturnRequestPage({ requestId }: { requestId: string }) {
    const router = useRouter();
    const { requestList } = useContext(BorrowRequestContext);
    const currentRequest = requestList
        .filter((rq) => rq.type == REQUEST_TYPE.RETURN)
        .find((r) => String(r.id) == requestId);
    if (currentRequest == undefined) {
        notFound();
    }

    const previousRequest = requestList.find(
        (rq) =>
            rq.type == REQUEST_TYPE.BORROW &&
            rq.borrowAt == currentRequest.borrowAt &&
            rq.state == REQUEST_STATE_TYPE.PERMIT,
    )!;

    const { item, borrowAt, returnAt, state, manager, response } = currentRequest;
    const { name: itemName } = item;

    let returnState;

    switch (state) {
        case REQUEST_STATE_TYPE.ASSIGNED:
            returnState = `검토중(담당자 : ${manager!.name})`;
            break;
        case REQUEST_STATE_TYPE.PENDING:
            returnState = "검토중(담당자 배정안됨)";
            break;
        case REQUEST_STATE_TYPE.PERMIT:
            returnState = "허가승인";
            break;
        default:
            returnState = "불허가";
            break;
    }
    return (
        <div className="mt-5">
            <p className="black-20px mb-3">✅ 기본정보</p>
            <InfoTable>
                <InfoRow label="요청번호" value={requestId} />
                <InfoRow label="대여물품" value={itemName} />
                <InfoRow label="대여일시" value={dateFormatter(borrowAt)} />
                <InfoRow label="반납일시" value={dateFormatter(returnAt)} />
                <InfoRow label="요청상태" value={returnState} />
                {response?.rejectReason != null ? <InfoRow label="불허가사유" value={response.rejectReason} /> : null}
            </InfoTable>
            <Button
                className="mt-4 w-full py-3 bold-16px"
                title="반납확인서 확인"
                onClick={() => router.push(`/return-list/${requestId}/paper`)}
            />
            <Button
                className="mt-2 w-full py-3 bold-16px bg-white! text-black! border border-black "
                title="이전 대여요청 확인"
                onClick={() => router.push(`/borrow-list/${previousRequest.id}`)}
            />
        </div>
    );
}
