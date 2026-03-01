export default function Spinner({ size, thickness }: { size: number; thickness: number }) {
    return (
        <div
            className="animate-spin rounded-full border-white/90 border-t-transparent"
            style={{
                width: size,
                height: size,
                borderWidth: thickness,
            }}
            aria-hidden="true"
        />
    );
}
