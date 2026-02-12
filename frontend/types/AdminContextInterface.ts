import AdminInfoInterface from "./AdminInfoInterface";

export default interface AdminContextInterface {
    adminInfo: AdminInfoInterface | null;
    setAdminInfo: React.Dispatch<React.SetStateAction<AdminInfoInterface>> | null;
}
