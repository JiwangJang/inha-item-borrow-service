"use client";

import BorrowRequestContext from "@/context/BorrowRequestContext";
import RequestInterface from "@/types/RequestInterface";
import { useState } from "react";

export default function BorrowRequestProvider({
    initialValue,
    children,
}: {
    initialValue: RequestInterface[];
    children: React.ReactNode;
}) {
    const [requestList, setRequestList] = useState(initialValue);
    return (
        <BorrowRequestContext.Provider
            value={{
                requestList,
                setRequestList,
            }}
        >
            {children}
        </BorrowRequestContext.Provider>
    );
}
