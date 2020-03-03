import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Package {

    private Map<String, Block> blocks;

    public Package() {
        this.blocks = new HashMap<String, Block>();
    }

    public Package(Map<String, Block> blocks) {
        this.blocks = blocks;
    }

    private Block getBlock(String blockName) {
        Block result = null;
        if (this.blocks != null) {
            String normalizedBlockName = normalizeName(blockName);
            result = this.blocks.get(normalizedBlockName);
        }
        return result;
    }

    public byte[] getContentBlock(String blockName) {
	Block block = getBlock(blockName);
	if (block != null) {
		return block.getContent();
	} else {
		return null;
	}
    }


    private void addBlock(Block block) {
	this.addBlock(block.getName(), block);
    }


    public void addBlock(String name, byte[] content) {
	this.addBlock(new Block(name, content));
    }

    private void addBlock(String blockName, Block block) {
        if (this.blocks == null) {
            this.blocks = new HashMap<String, Block>();
        }
        String normalizedBlockName = normalizeName(blockName);
        this.blocks.put(normalizedBlockName, block);
    }

    public void updateBlock(String name, byte[] content) {
        Block block = new Block(name, content);
        if (this.blocks != null && this.blocks.containsKey(block.getName())) {
            this.blocks.replace(block.getName(), block);
        }
        else {
	    this.addBlock(block.getName(), block);
       }
    }


    public void deleteBlock(String blockName) {
        if (this.blocks != null) {
            if (this.blocks.containsKey(blockName)) {
                this.blocks.remove(blockName);
            }
        }
    }

    public List<String> getBlockNames() {
        List<String> result = new ArrayList<String>(this.blocks.keySet());
        Collections.sort(result);
        return result;
    }

    private String normalizeName(String blockName) {
        String result = blockName.trim().replaceAll(" ", "_").toUpperCase();
        return result;
    }
}

