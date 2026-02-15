"use client";

import AdminStudentCouncilFeeContext from "@/context/AdminStudentCouncilFeeContext";
import { useContext, useState } from "react";
import StudentCouncilFeeFilter from "./StudentCouncilFeeFilter";
import StudentCouncilFeeCard from "./StudentCouncilFeeCard";
import StudentCouncilFeeVerificationInterface from "@/types/StudentCouncilFeeVerificationInterface";
import { useRouter } from "next/navigation";

export default function StudentCouncilFeePage() {
    const studentCouncilFeeVerification = useContext(AdminStudentCouncilFeeContext);
    const [selectType, setSelectType] = useState("전체");
    const filtered: StudentCouncilFeeVerificationInterface[] = [];
    const router = useRouter();

    studentCouncilFeeVerification.studentCouncilFeeList.forEach((elem) => {
        if (selectType == "승인") {
            if (elem.verify) filtered.push(elem);
        } else if (selectType == "미승인") {
            if (!elem.verify) filtered.push(elem);
        } else {
            filtered.push(elem);
        }
    });

    return (
        <div className="mt-5">
            <div>
                <p className="black-20px mb-1">등록금 인증신청 목록</p>
                <p className="regular-14px">카드를 클릭하시면 납부인증사진 확인페이지로 넘어갑니다.</p>
            </div>
            <div className="flex gap-1 mt-2">
                {["전체", "승인", "미승인"].map((s, i) => (
                    <StudentCouncilFeeFilter
                        key={i}
                        name={s}
                        onClick={(v) => setSelectType(s)}
                        isSelect={selectType == s}
                    />
                ))}
            </div>
            {filtered.length == 0 ? (
                <div className="mt-3 text-center">등록된 학생회비 납부 인증신청이 없습니다</div>
            ) : (
                <div className="mt-3 grid grid-cols-3 gap-1">
                    {filtered.map((f, key) => (
                        <StudentCouncilFeeCard
                            borrowerName={f.borrowerName}
                            borrowerId={f.borrowerId}
                            requestAt={f.requestAt}
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
