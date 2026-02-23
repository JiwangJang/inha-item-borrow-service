"use client";

import DivisionContext from "@/context/DivisionContext";
import DivisionInterface from "@/types/DivisionInterface";
import { useRef, useState } from "react";

export default function DivisionProvider({
    initialValue,
    children,
}: {
    initialValue: DivisionInterface[];
    children: React.ReactNode;
}) {
    const [divisionList, setDivisionList] = useState(initialValue);
    return <DivisionContext.Provider value={{ divisionList, setDivisionList }}>{children}</DivisionContext.Provider>;
}
