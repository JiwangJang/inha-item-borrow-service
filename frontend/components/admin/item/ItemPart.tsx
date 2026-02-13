import ItemInterface from "@/types/ItemInterface";
import EachItem from "./EachItem";
import Link from "next/link";

export default function ItemPart({ name, items }: { name: string; items: ItemInterface[] }) {
    return (
        <div className="mt-3">
            <div className="flex items-center mb-2 gap-2">
                <p className="bold-18px">
                    {name} (총{items.length}개)
                </p>
                <div className="w-full h-0.5 bg-slate-950 flex-1" />
            </div>

            <div className="grid grid-cols-3 gap-2">
                {items.map((it) => (
                    <Link href={`/admin/item/${it.id}`} key={it.id}>
                        <EachItem id={it.id} status={it.status} />
                    </Link>
                ))}
            </div>
        </div>
    );
}
