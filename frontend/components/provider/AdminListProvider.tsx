"use client";

import AdminListContext from "@/context/AdminListContext";
import AdminInfoInterface from "@/types/AdminInfoInterface";
import React, { useState } from "react";

export default function AdminListProvider({
    initialValue,
    children,
}: {
    initialValue: AdminInfoInterface[];
    children: React.ReactNode;
}) {
    const [adminList, setAdminList] = useState(initialValue);
    return <AdminListContext.Provider value={{ adminList, setAdminList }}>{children}</AdminListContext.Provider>;
}
