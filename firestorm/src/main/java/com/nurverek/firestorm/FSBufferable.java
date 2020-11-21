package com.nurverek.firestorm;

import com.nurverek.vanguard.VLBufferAddress;
import com.nurverek.vanguard.VLBufferManagerBase;

public interface FSBufferable<ADDRESS extends VLBufferAddress>{

    void bind(ADDRESS address);
}
