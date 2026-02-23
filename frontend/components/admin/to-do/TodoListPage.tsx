"use client";

import Filter from "@/components/utilities/Filter";
import AdminContext from "@/context/AdminContext";
import AdminRequestContext from "@/context/AdminRequestContext";
import { REQUEST_TYPE } from "@/types/RequestInterface";
import { useContext, useState } from "react";
import TodoInfoCard from "./TodoInfoCard";

export default function TodoListPage() {
    const { requestList } = useContext(AdminRequestContext);
    const { adminInfo } = useContext(AdminContext);
    const [select, setSelect] = useState("전체");

    const filtered = requestList
        .filter((rq) => {
            switch (select) {
                case "대여":
                    return rq.manager!.id == adminInfo!.id && rq.type == REQUEST_TYPE.BORROW;
                case "반납":
                    return rq.manager!.id == adminInfo!.id && rq.type == REQUEST_TYPE.RETURN;
                default:
                    return rq.manager!.id == adminInfo!.id;
            }
        })
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    return (
        <div className="mt-5">
            <p className="black-20px">나의 할일</p>
            <div className="mt-3">
                <Filter curValue={select} labels={["전체", "대여", "반납"]} onClick={(value) => setSelect(value)} />
            </div>
            {filtered.length == 0 ? (
                <p className="mt-2">처리할(한) 요청이 없습니다,</p>
            ) : (
                <div className="my-3 flex flex-col gap-1">
                    {filtered.map((rq, i) => (
                        <TodoInfoCard
                            key={i}
                            requestId={rq.id}
                            borrowAt={rq.borrowAt}
                            retrunAt={rq.returnAt}
                            requestAt={rq.createdAt}
                            itemName={rq.item.name}
                            requesterId={rq.borrowerId}
                            requesterName={rq.borrowerName}
                            type={rq.type}
                            state={rq.state}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
