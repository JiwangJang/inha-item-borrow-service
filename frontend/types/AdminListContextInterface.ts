import AdminInfoInterface from "./AdminInfoInterface";

export default interface AdminListContextInterface {
    adminList: AdminInfoInterface[];
    setAdminList: React.Dispatch<React.SetStateAction<AdminInfoInterface[]>> | null;
}
