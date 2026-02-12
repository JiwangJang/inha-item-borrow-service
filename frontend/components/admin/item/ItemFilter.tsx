import { MouseEventHandler } from "react";

export default function ItemFilter({
    name,
    isSelect,
    onClick,
}: {
    name: string;
    isSelect: boolean;
    onClick: (name: string) => void;
}) {
    return (
        <div
            className="border border-black rounded-full px-3 py-0.5 cursor-pointer transition"
            style={{
                backgroundColor: isSelect ? "black" : "white",
                color: isSelect ? "white" : "black",
            }}
            onClick={() => {
                onClick(name);
            }}
        >
            {name}
        </div>
    );
}
