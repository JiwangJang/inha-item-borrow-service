"use client";

import AdminRequestContext from "@/context/AdminRequestContext";
import RequestInterface from "@/types/RequestInterface";
import { useState } from "react";

export default function AdminRequestProvider({
    initialValue,
    children,
}: {
    initialValue: RequestInterface[];
    children: React.ReactNode;
}) {
    const [requestList, setRequestList] = useState(initialValue);
    return (
        <AdminRequestContext.Provider
            value={{
                requestList,
                setRequestList,
            }}
        >
            {children}
        </AdminRequestContext.Provider>
    );
}
