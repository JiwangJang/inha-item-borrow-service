import ItemInterface from "./ItemInterface";

export default interface ItemContextInterface {
    itemList: ItemInterface[];
    setItemList: React.Dispatch<React.SetStateAction<ItemInterface[]>> | null;
}
