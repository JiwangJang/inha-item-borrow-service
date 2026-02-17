"use client";

import BorrowRequestContext from "@/context/BorrowRequestContext";
import { REQUEST_TYPE } from "@/types/RequestInterface";
import { useContext } from "react";
import RequestInfoCard from "./RequestInfoCard";
import BorrowerContext from "@/context/BorrowerContext";
import LoginRequired from "../LoginRequired";

export default function BorrowListPage() {
    const borrowerInfo = useContext(BorrowerContext).borrowerInfo;
    if (borrowerInfo == null) {
        return <LoginRequired />;
    }

    const requestList = useContext(BorrowRequestContext)
        .requestList.filter((r) => r.type == REQUEST_TYPE.BORROW)
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    return (
        <div className="my-5">
            <p className="black-20px mb-3">🗒️ 대여내역</p>

            {requestList.length == 0 ? (
                <p>아직 신청을 한번도 안하셔서 기록이 없습니다.</p>
            ) : (
                <div className="flex flex-col gap-2">
                    {requestList.map((r, i) => (
                        <RequestInfoCard
                            cancel={r.cancel}
                            borrowAt={r.borrowAt}
                            itemName={r.item.name}
                            requestAt={r.createdAt}
                            requestId={r.id}
                            retrunAt={r.returnAt}
                            state={r.state}
                            type={r.type}
                            key={i}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
