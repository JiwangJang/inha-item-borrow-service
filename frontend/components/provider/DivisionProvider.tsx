"use client";

import DivisionContext from "@/context/DivisionContext";
import DivisionInterface from "@/types/DivisionInterface";

export default function DivisionProvider({
    initialValue,
    children,
}: {
    initialValue: DivisionInterface[];
    children: React.ReactNode;
}) {
    return <DivisionContext.Provider value={initialValue}>{children}</DivisionContext.Provider>;
}
