import AdminListContextInterface from "@/types/AdminListContextInterface";
import { createContext } from "react";

const AdminListContext = createContext<AdminListContextInterface>({
    adminList: [],
    setAdminList: null,
});

export default AdminListContext;
