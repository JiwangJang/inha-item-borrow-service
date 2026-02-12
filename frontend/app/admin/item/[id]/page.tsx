import SingleItemViewPage from "@/components/admin/item/single/SingleItemViewPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;
    return <SingleItemViewPage id={id} />;
}
