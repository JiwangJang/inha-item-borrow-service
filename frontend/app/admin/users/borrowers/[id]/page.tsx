import SearchedBorrowerPage from "@/components/admin/users/borrowers/single/SearchedBorrowerPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <SearchedBorrowerPage borrowerId={id} />;
}
