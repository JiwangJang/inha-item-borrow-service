import RequestPaperPage from "@/components/borrower/borrow-list/single/paper/RequestPaperPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <RequestPaperPage id={id} />;
}
