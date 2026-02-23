"use client";

import DivisionContext from "@/context/DivisionContext";
import { useContext, useState } from "react";
import DivisionCard from "./DivisionCard";
import Button from "@/components/utilities/Button";
import { useRouter } from "next/navigation";

export default function DivisoinPage() {
    const { divisionList, setDivisionList } = useContext(DivisionContext)!;
    const router = useRouter();

    return (
        <div className="mt-5">
            <p className="black-20px">🏬 부서관리</p>
            <div className="mt-2 grid grid-cols-3 gap-1">
                {divisionList.map(({ name, code }) => (
                    <DivisionCard name={name} code={code} key={code} />
                ))}
            </div>
            <div className="fixed max-w-125 left-1/2 translate-x-[-50%] bottom-0 w-full px-4 pb-5">
                <Button
                    title="신규 부서추가"
                    className="w-full py-3 bold-18px"
                    onClick={() => router.push("/admin/division/new")}
                />
            </div>
        </div>
    );
}
