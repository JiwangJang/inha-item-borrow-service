"use client";

import AdminContextInterface from "@/types/AdminContextInterface";
import { createContext } from "react";

const AdminContext = createContext<AdminContextInterface>({
    adminInfo: null,
    setAdminInfo: null,
});

export default AdminContext;
