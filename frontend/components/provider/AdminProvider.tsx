"use client";

import AdminContext from "@/context/AdminContext";
import AdminInfoInterface from "@/types/AdminInfoInterface";
import React, { useState } from "react";

export default function AdminProvider({
    initialValue,
    children,
}: {
    initialValue: AdminInfoInterface;
    children: React.ReactNode;
}) {
    const [adminInfo, setAdminInfo] = useState<AdminInfoInterface | null>(initialValue);
    return <AdminContext value={{ adminInfo, setAdminInfo }}>{children}</AdminContext>;
}
