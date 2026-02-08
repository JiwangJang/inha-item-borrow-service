"use client";

import BorrowerContext from "@/context/BorrowerContext";
import BorrowerInfoInterface from "@/types/BorrowerInfoInterface";
import BorrowerContextInterface from "@/types/BorrowerInfoInterface";
import React, { useState } from "react";

export default function BorrowerProvider({
    initialBorrowerInfo,
    children,
}: {
    initialBorrowerInfo: BorrowerInfoInterface | null;
    children: React.ReactNode;
}) {
    const [borrowerInfo, setBorrowerInfo] = useState<BorrowerContextInterface | null>(initialBorrowerInfo);

    return <BorrowerContext.Provider value={{ borrowerInfo, setBorrowerInfo }}>{children}</BorrowerContext.Provider>;
}
