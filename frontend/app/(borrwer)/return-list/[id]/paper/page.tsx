import ReturnRequestPaperPage from "@/components/borrower/return-list/single/paper/ReturnRequestPaperPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <ReturnRequestPaperPage requestId={id} />;
}
