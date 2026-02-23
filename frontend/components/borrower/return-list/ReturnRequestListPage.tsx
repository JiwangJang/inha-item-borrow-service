"use client";

import BorrowRequestContext from "@/context/BorrowRequestContext";
import { REQUEST_TYPE } from "@/types/RequestInterface";
import { useContext } from "react";
import RequestInfoCard from "../borrow-list/BorrowRequestInfoCard";
import BorrowerContext from "@/context/BorrowerContext";
import LoginRequired from "../LoginRequired";

export default function ReturnRequestListPage() {
    const borrowerInfo = useContext(BorrowerContext).borrowerInfo;
    if (borrowerInfo == null) {
        return <LoginRequired />;
    }
    const { requestList } = useContext(BorrowRequestContext);
    const returnRequestList = requestList
        .filter((rq) => rq.type == REQUEST_TYPE.RETURN)
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    return (
        <div className="mt-5">
            <p className="black-20px">🗒️ 반납신청내역</p>
            <div className="mt-3">
                {returnRequestList.length == 0 ? (
                    <p>반납신청 내역이 없습니다.</p>
                ) : (
                    <div className="flex flex-col gap-1">
                        {returnRequestList.map((rq) => (
                            <RequestInfoCard
                                key={rq.id}
                                requestId={rq.id}
                                borrowAt={rq.borrowAt}
                                cancel={rq.cancel}
                                itemName={rq.item.name}
                                requestAt={rq.createdAt}
                                retrunAt={rq.returnAt}
                                state={rq.state}
                                type={rq.type}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
