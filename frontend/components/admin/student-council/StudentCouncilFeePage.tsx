"use client";

import AdminStudentCouncilFeeContext from "@/context/AdminStudentCouncilFeeContext";
import { useContext, useState } from "react";
import StudentCouncilFeeCard from "./StudentCouncilFeeCard";
import StudentCouncilFeeVerificationInterface from "@/types/StudentCouncilFeeVerificationInterface";
import { useRouter } from "next/navigation";
import Filter from "@/components/utilities/Filter";

export default function StudentCouncilFeePage() {
    const { studentCouncilFeeList } = useContext(AdminStudentCouncilFeeContext);
    const [selectType, setSelectType] = useState("전체");
    const filtered: StudentCouncilFeeVerificationInterface[] = [];
    const router = useRouter();

    studentCouncilFeeList.forEach((elem) => {
        if (selectType == "승인") {
            if (elem.verify) filtered.push(elem);
        } else if (selectType == "미승인") {
            if (!elem.verify) filtered.push(elem);
        } else {
            filtered.push(elem);
        }
    });

    filtered.sort((a, b) => new Date(b.requestAt!).getTime() - new Date(a.requestAt!).getTime());

    return (
        <div className="mt-5">
            <div>
                <p className="black-20px mb-1">학생회비 인증신청 목록</p>
                <p className="regular-14px">카드를 클릭하시면 납부인증사진 확인페이지로 넘어갑니다.</p>
            </div>
            <div className="flex gap-1 mt-2">
                <Filter
                    curValue={selectType}
                    labels={["전체", "승인", "미승인"]}
                    onClick={(v: string) => setSelectType(v)}
                />
            </div>
            {filtered.length == 0 ? (
                <div className="mt-3 text-center">등록된 학생회비 납부 인증신청이 없습니다</div>
            ) : (
                <div className="mt-3 grid grid-cols-2 gap-1">
                    {filtered.map((f, key) => (
                        <StudentCouncilFeeCard
                            borrowerName={f.borrowerName}
                            borrowerId={f.borrowerId}
                            requestAt={f.requestAt!}
                            verify={f.verify!}
                            onClick={() => router.push(`/admin/student-council-fee/${f.id}`)}
                            key={key}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
