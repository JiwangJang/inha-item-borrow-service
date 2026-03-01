import ManagerInfoPage from "@/components/admin/users/managers/single/ManagerInfoPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <ManagerInfoPage adminId={id} />;
}
