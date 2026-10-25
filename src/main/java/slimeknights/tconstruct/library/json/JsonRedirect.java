package slimeknights.tconstruct.library.json;

import com.google.gson.JsonObject;
import lombok.Data;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;

/** Represents a redirect in a material or modifier JSON */
@SuppressWarnings("ClassCanBeRecord") // GSON does not support records
@Data
public class JsonRedirect {
  private final ResourceLocation id;
  @Nullable
  private final ICondition condition;

  /** Serializes this to JSON */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("id", id.toString());
    if (condition != null) {
      json.add("condition", JsonHelper.serialize(ICondition.CODEC, condition));
    }
    return json;
  }

  /** Deserializes this to JSON */
  public static JsonRedirect fromJson(JsonObject json) {
    ResourceLocation id = JsonHelper.getResourceLocation(json, "id");
    ICondition condition = null;
    if (json.has("condition")) {
      condition = JsonHelper.parse(ICondition.CODEC, json.get("condition"));
    }
    return new JsonRedirect(id, condition);
  }
}
