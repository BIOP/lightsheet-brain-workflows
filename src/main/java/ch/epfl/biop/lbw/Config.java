package ch.epfl.biop.lbw;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class Config {

    public General general;
    public BigStitcher bigstitcher;
    public BrainReg brainreg;

    public static Set<String> parametersToSkip() {
        HashSet<String> set = new HashSet<>();
        set.add("output dir");
        set.add("input file");
        set.add("xml file");
        set.add("docker container name");
        return set;
    }

    public static Set<Class<?>> simpleClasses() {
        HashSet<Class<?>> set = new HashSet<>();
        set.add(String.class);
        set.add(Boolean.class);
        set.add(Double.class);
        set.add(Integer.class);
        return set;
    }

    public static class General {
        public String user;
        public String save_dir;
        public String output_dir; // Parameters not present in template
        public String input_file; // Parameters not present in template
        //output_dir, fuse_dir, input_file
    }

    public static class BigStitcher {
        public String xml_file;
        public String reader;
        public String fusion; // fusion type ? String ? Boolean
        public ChannelAlignment channel_alignment;
        public TileAlignment tile_alignment;
        public ICPRefinement icp_refinement;
        public Reorientation reorientation;
        public FusionConfig fusion_config;

        public static class ChannelAlignment {
            public PairwiseShiftsDownsamples pairwise_shifts_downsamples;
            public Double filter_min_r;

            public static class PairwiseShiftsDownsamples {
                public Integer x;
                public Integer y;
                public Integer z;
            }
        }

        public static class TileAlignment {
            public Integer use_channel;
            public ChannelAlignment.PairwiseShiftsDownsamples pairwise_shifts_downsamples;
            public Double filter_min_r;
        }

        public static class ICPRefinement {
            public String icp_refinement_type;
            public String downsampling;
            public String interest;
            public String icp_max_error;
        }

        public static class Reorientation {
            public String raw_orientation;
            public Boolean reorient_to_asr;
        }

        public static class FusionConfig {
            public String fusion_type;
            public Integer downsampling;
            public Boolean preserve_original;
            public String produce;
            public String fused_image;
            public String fusion_method;
            public String fuse_dir;
        }
    }

    public static class BrainReg {
        public String docker_container_name;
        public String conda_environement_name;
        public String conda_activate_path;
        public Parameters parameters;

        public static class Parameters {
            public String atlas;
            public String backend;
            public Integer affine_n_steps;
            public Integer affine_use_n_steps;
            public Integer freeform_n_steps;
            public Integer freeform_use_n_steps;
            public Double bending_energy_weight;
            public Integer grid_spacing;
            public Double smoothing_sigma_reference;
            public Double smoothing_sigma_floating;
            public Integer histogram_n_bins_floating;
            public Integer histogram_n_bins_reference;
            public Integer n_free_cpus;
            public Boolean debug;
            public Boolean save_original_orientation;
            public String brain_geometry;
            public Boolean sort_input_file;
            public String pre_processing;
        }

        public static String getParamsAsString(Parameters p) {
            StringJoiner args = new StringJoiner(" ");
            Arrays.stream(Parameters.class.getDeclaredFields()).forEach(
                    f -> {
                        try {
                            if (f.get(p) != null) {
                                if (f.getType().equals(Boolean.class) && ((Boolean) f.get(p))) {
                                    args.add("--"+f.getName());
                                } else {
                                    args.add("--"+f.getName());
                                    args.add(f.get(p).toString());
                                }
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            return args.toString();
        }

    }

    public static Config loadFromFile(File yamlFile) throws IOException {
        String yamlContent = new String(Files.readAllBytes(yamlFile.toPath()));
        //Yaml yaml = new Yaml();
        LoaderOptions loaderoptions = new LoaderOptions();
        TagInspector taginspector =
                tag -> tag.getClassName().equals(Config.class.getName());
        loaderoptions.setTagInspector(taginspector);
        Yaml yaml = new Yaml(new Constructor(Config.class, loaderoptions));
        return yaml.loadAs(yamlContent, Config.class);
    }

    public static Map<String, Object> getConfigObjectAsUntypedTree(Config config) {
        return recursiveFill(new LinkedHashMap<>(), config);
    }

    private static Map<String, Object> recursiveFill(Map<String, Object> map, Object object) {
        Arrays.stream(object.getClass().getDeclaredFields()).forEach(field -> {
            try {
                // Collect the field of the object;
                Object o = field.get(object);
                if   ( (o== null)
                   || o.getClass().equals(String.class)
                   || o.getClass().equals(Integer.class)
                   || o.getClass().equals(Double.class)
                   || o.getClass().equals(Boolean.class)) {
                    map.put(field.getName(), o);
                } else {
                    map.put(field.getName(), recursiveFill(new LinkedHashMap<>(), o));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    public static void main(String... args) throws Exception {

        Config config = Config.loadFromFile(new File("parameters_axel.yml"));
        System.out.println(new GsonBuilder().setPrettyPrinting().create()
                .toJson(getConfigObjectAsUntypedTree(config)));

        new YamlGUI().myDialog(config);
    }

}
