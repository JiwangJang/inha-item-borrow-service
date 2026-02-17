import TodoSinglePage from "@/components/admin/to-do/single/TodoSinglePage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <TodoSinglePage id={id} />;
}
