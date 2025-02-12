package net.polarfox27.jobs.gui.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.polarfox27.jobs.data.ServerJobsData;
import net.polarfox27.jobs.data.capabilities.PlayerData;
import net.polarfox27.jobs.data.capabilities.PlayerJobs;

import javax.annotation.Nonnull;

public class ContainerCraft extends Container {
	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	private final World world;
	private final BlockPos pos;
	private final EntityPlayer player;

	public ContainerCraft(InventoryPlayer playerInventory, World worldIn, BlockPos posIn) {
		this.world = worldIn;
		this.pos = posIn;
		this.player = playerInventory.player;
		this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35));

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
			}
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for (int l = 0; l < 9; ++l) {
			this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 142));
		}
	}

	@Override
	protected void slotChangedCraftingGrid(World world, @Nonnull EntityPlayer player, @Nonnull InventoryCrafting grid,
										   @Nonnull InventoryCraftResult res)
	{
		if (!world.isRemote) {
			EntityPlayerMP entityplayermp = (EntityPlayerMP)player;
			PlayerJobs jobs = PlayerData.getPlayerJobs(player);
			ItemStack itemstack = ItemStack.EMPTY;
			IRecipe irecipe = CraftingManager.findMatchingRecipe(grid, world);

			if (irecipe != null && (irecipe.isDynamic() ||
					!world.getGameRules().getBoolean("doLimitedCrafting") ||
					entityplayermp.getRecipeBook().isUnlocked(irecipe))) {
				res.setRecipeUsed(irecipe);
				itemstack = irecipe.getCraftingResult(grid);
				if(ServerJobsData.BLOCKED_CRAFTS.isBlocked(jobs, itemstack)){
					itemstack = ItemStack.EMPTY;
				}
			}
			res.setInventorySlotContents(0, itemstack);
			entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
		}
	}

	public void onCraftMatrixChanged(@Nonnull IInventory inventory) {
		this.slotChangedCraftingGrid(this.world, this.player, this.craftMatrix, this.craftResult);
	}

	public void onContainerClosed(@Nonnull EntityPlayer player) {
		super.onContainerClosed(player);

		if (!this.world.isRemote) {
			this.clearContainer(player, this.world, this.craftMatrix);
		}
	}

	public boolean canInteractWith(@Nonnull EntityPlayer player) {
		if (this.world.getBlockState(this.pos).getBlock() != Blocks.CRAFTING_TABLE) {
			return false;
		}
		else {
			return player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Nonnull
	public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 0) {
				itemstack1.getItem().onCreated(itemstack1, this.world, player);

				if (!this.mergeItemStack(itemstack1, 10, 46, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			else if (index >= 10 && index < 37) {
				if (!this.mergeItemStack(itemstack1, 37, 46, false)) {
					return ItemStack.EMPTY;
				}
			}
			else if (index >= 37 && index < 46) {
				if (!this.mergeItemStack(itemstack1, 10, 37, false)) {
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 10, 46, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			}
			else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			ItemStack itemstack2 = slot.onTake(player, itemstack1);

			if (index == 0) {
				player.dropItem(itemstack2, false);
			}
		}

		return itemstack;
	}

	public boolean canMergeSlot(@Nonnull ItemStack stack, Slot slotIn) {
		return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
	}
}
