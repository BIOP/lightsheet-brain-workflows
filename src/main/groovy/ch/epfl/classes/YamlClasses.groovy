package ch.epfl.classes

public class YamlParameters {
    GlobalVariables global_variables;
    GeneralParameters general_parameters;
    ResavingParameters resaving_parameters;
    ChannelParameters channel_alignment_parameters;
    TileParameters tile_alignment_parameters;
    IcpRefinementParameters icp_refinement_parameters;
    FusionParameters fusion_parameters;
}

public class GlobalVariables {
    List<String> user_list;
    String raw_data_server;
    String analysis_server;
}

public class GeneralParameters {
    String user;
    String input_path;
    String output_dir;
    String parent_yaml_parameter_file;
    String processing_yaml_parameter_file;
    boolean save_2D;
    boolean save_3D;
    boolean preprocessing;
    boolean atlas_registration;
    List<String> brain_orientation;
    String downsampling;
}

public class ResavingParameters {
    List<String> subsampling_factors;
    List<String> hdf5_chunk_sizes;
}

public class ChannelParameters {
    List pairwise_shifts_downsamples_XYZ;
    float filter_min_r;
}

public class TileParameters {
    List pairwise_shifts_parameters;
    float filter_min_r;
    String	optimize_fix_group;
}

public class IcpRefinementParameters {
    String icp_refinement_type;
    String downsampling;
    String interest;
    String icp_max_error;
}

public class FusionParameters {
    String preserve_original;
    String produce;
    String fused_image;
    String filename_addition;
}