package VendingMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private final Map<String, ItemSlot> slotsById = new HashMap<>();

    void addSlot(ItemSlot itemSlot) {
        slotsById.put(itemSlot.getSlotId(), itemSlot);
    }

    ItemSlot getSlot(String slotId) {
        return slotsById.get(slotId);
    }

    Collection<ItemSlot> getAllSlots() {
        return slotsById.values();
    }
}
