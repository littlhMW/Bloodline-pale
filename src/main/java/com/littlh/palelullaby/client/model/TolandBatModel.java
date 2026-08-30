package com.littlh.palelullaby.client.model;

import com.littlh.palelullaby.entity.TolandBatEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 巨蝙蝠托兰的占位模型：站立的大蝙蝠，展开双翼，红色眼睛。
 * 几何参照原版蝙蝠模型放大改造成直立姿态，纹理 64x64。
 */
public class TolandBatModel extends HierarchicalModel<TolandBatEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("pale_lullaby", "toland_bat"), "main");

    private final ModelPart root;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public TolandBatModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.root = root;
        this.rightWing = root.getChild("body").getChild("right_wing");
        this.leftWing = root.getChild("body").getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 26.0F, 0.0F));
        root.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(-2.0F, 31.0F, -2.5F));
        root.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(5, 30).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(2.0F, 31.0F, -2.5F));
        root.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, 0.0F, -1.0F, 1.5F, 1.5F, 1.0F),
                PartPose.offset(-2.0F, 28.0F, -3.5F));
        root.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(0, 35).addBox(-0.5F, 0.0F, -1.0F, 1.5F, 1.5F, 1.0F),
                PartPose.offset(2.0F, 28.0F, -3.5F));
        PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(28, 0).addBox(-4.0F, -1.0F, 0.0F, 4.0F, 14.0F, 1.0F),
                PartPose.offset(-5.0F, 4.0F, 0.0F));
        rightWing.addOrReplaceChild("right_wing_tip",
                CubeListBuilder.create().texOffs(28, 15).addBox(-12.0F, -1.0F, 0.0F, 12.0F, 16.0F, 1.0F),
                PartPose.offset(-4.0F, 0.0F, 0.0F));
        PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(44, 0).addBox(0.0F, -1.0F, 0.0F, 4.0F, 14.0F, 1.0F),
                PartPose.offset(5.0F, 4.0F, 0.0F));
        leftWing.addOrReplaceChild("left_wing_tip",
                CubeListBuilder.create().texOffs(44, 15).addBox(0.0F, -1.0F, 0.0F, 12.0F, 16.0F, 1.0F),
                PartPose.offset(4.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("feet",
                CubeListBuilder.create().texOffs(28, 32).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(TolandBatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float flap = Mth.sin(ageInTicks * 0.35F) * 0.35F;
        this.rightWing.zRot = flap;
        this.leftWing.zRot = -flap;
    }
}
