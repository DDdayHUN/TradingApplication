import type {ComponentType, ReactElement} from "react";

interface ListItem {
    id: string;
}

export interface ListProps<T extends ListItem> {
    elements: T[];
    RowComponent: ComponentType<{item: T}>
    flexDirection?: string;
    maxLength?: number;
}

export default function ListLayout<T extends ListItem>({elements, RowComponent, flexDirection, maxLength}: ListProps<T>): ReactElement {
    if(!elements || elements.length === 0) {
        return (
            <>
            </>
        );
    }

    return (
        <div className={`flex ${flexDirection} flex-wrap  w-full h-full`}>
            {(maxLength !== undefined ? elements.slice(0, maxLength ?? elements.length): elements).map((item, index ) => (
                <RowComponent
                    key={item.id ?? index}
                    item={item}
                />
            ))}
        </div>
    )
}