package com.elders.imidburn.adapters;

import com.elders.imidburn.Constants;

/**
 * Created by davidbrodsky on 6/15/15.
 */
public interface AdapterListener {
    void onItemSelected(int modelId, Constants.PlayaItemType type);
    void onItemFavoriteButtonSelected(int modelId, Constants.PlayaItemType type);

}
