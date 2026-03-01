"use client";

import ManagerCard from "./ManagerCard";
import { useContext } from "react";
import AdminListContext from "@/context/AdminListContext";
import Button from "@/components/utilities/Button";
import { useRouter } from "next/navigation";

export default function ManagerListPage() {
    const { adminList } = useContext(AdminListContext);
    const router = useRouter();

    return (
        <div className="mt-5">
            <div>
                <p className="black-20px">🧑‍✈️ 관리자 목록</p>
                <p>동료들과 같이 아름다운 학생회를 만들어보아요!</p>
            </div>
            <div className="mt-3 mb-4 flex flex-col gap-1">
                {adminList.map((admin) => (
                    <ManagerCard adminInfo={admin} key={admin.id} />
                ))}
            </div>
            <div className="fixed left-1/2 bottom-0 w-full max-w-125 translate-x-[-50%] px-5 pb-5 pointer-events-none">
                <div className="pointer-events-auto">
                    <Button
                        className="w-full py-3 bold-18px"
                        title="새 관리자 추가"
                        onClick={() => router.push("/admin/users/managers/new")}
                    />
                </div>
            </div>
        </div>
    );
}
