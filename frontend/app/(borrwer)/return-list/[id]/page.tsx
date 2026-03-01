import SingleReturnRequestPage from "@/components/borrower/return-list/single/SingleReturnRequestPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <SingleReturnRequestPage requestId={id} />;
}
