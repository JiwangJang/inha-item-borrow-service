"use client";

import AdminStudentCouncilFeeContext from "@/context/AdminStudentCouncilFeeContext";
import StudentCouncilFeeInterface from "@/types/StudentCouncilFeeVerificationInterface";
import { useState } from "react";

export default function AdminStudentCouncilFeeProvider({
    initialValue,
    children,
}: {
    initialValue: StudentCouncilFeeInterface[];
    children: React.ReactNode;
}) {
    const [studentCouncilFeeList, setStudentCouncilFeeList] = useState(initialValue);
    return (
        <AdminStudentCouncilFeeContext.Provider value={{ studentCouncilFeeList, setStudentCouncilFeeList }}>
            {children}
        </AdminStudentCouncilFeeContext.Provider>
    );
}
