import type {ComponentType, ReactElement} from "react";

interface ListItem {
    id: string;
}

export interface ListProps<T extends ListItem> {
    elements: T[];
    RowComponent: ComponentType<{item: T}>
}

export default function ListLayout<T extends ListItem>({elements, RowComponent}: ListProps<T>): ReactElement {
    if(!elements || elements.length === 0) {
        return (
            <>
            </>
        );
    }

    return (
        <div className="flex flex-col flex-wrap justify-around w-full h-full">
            {elements.map((item, index ) => (
                <RowComponent
                    key={item.id ?? index}
                    item={item}
                />
            ))}
        </div>
    )
}