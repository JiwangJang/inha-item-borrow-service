import FilterItem from "./FilterItem";

export default function Filter({
    labels,
    curValue,
    onClick,
}: {
    labels: string[];
    curValue: string;
    onClick: (name: string) => void;
}) {
    return (
        <div className="flex gap-1">
            {labels.map((label, i) => (
                <FilterItem name={label} isSelect={label == curValue} onClick={onClick} key={i} />
            ))}
        </div>
    );
}
