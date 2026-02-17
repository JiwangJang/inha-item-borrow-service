"use client";

import Filter from "@/components/utilities/Filter";
import AdminRequestContext from "@/context/AdminRequestContext";
import { useContext, useState } from "react";
import AdminRequestInfoCard from "./AdminRequestInfoCard";
import { REQUEST_TYPE } from "@/types/RequestInterface";

export default function RequestListPage() {
    const { requestList, setRequestList } = useContext(AdminRequestContext);
    const [select, setSelect] = useState("전체");
    const filtered = requestList.filter((rq) => {
        switch (select) {
            case "대여":
                return rq.manager!.id == null && rq.type == REQUEST_TYPE.BORROW;
            case "반납":
                return rq.manager!.id == null && rq.type == REQUEST_TYPE.RETURN;
            default:
                return rq.manager!.id == null;
        }
    });

    return (
        <div className="mt-5">
            <p className="black-20px">신청목록</p>
            <div className="mt-3">
                <Filter curValue={select} labels={["전체", "대여", "반납"]} onClick={(v) => setSelect(v)} />
            </div>
            <div className="my-3 flex flex-col gap-1">
                {filtered.length == 0 ? (
                    <p>처리할 요청이 없습니다.</p>
                ) : (
                    filtered.map((f) => (
                        <AdminRequestInfoCard
                            requesterId={f.borrowerId}
                            requesterName={f.borrowerName}
                            key={f.id}
                            requestId={f.id}
                            borrowAt={f.borrowAt}
                            itemName={f.item.name}
                            requestAt={f.createdAt}
                            retrunAt={f.returnAt}
                            type={f.type}
                        />
                    ))
                )}
            </div>
        </div>
    );
}
