import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.*
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
//import ch.epfl.classes.*
import java.util.regex.Matcher
import java.util.regex.Pattern

import ij.IJ

public class GUIGeneration{
    // General variables for the GUI
    private JFrame frame
    private JPanel panel
    private JScrollPane scrollPane
    private int width
    private int height
    private boolean show_param
    private int gbc_ypos
    private File output_folder_generated
    private File analysis_folder_path

    // Variables for the YAML parameter file selection
    private JLabel label_yaml_param
    private JTextField disp_yaml_param
    private JButton button_select_yaml
    private JButton button_edit_yaml
    private JFileChooser yaml_chooser
    private YamlParameters root_yaml_parameters // Will store the variables found in the Yaml properties file.
    private YamlParameters edited_yaml_parameters
    private ObjectMapper om

    // ---- YAML Global Variables ----
    private JLabel users_label
    private JComboBox<String> users_list
    private JButton button_add_users

    // Variables for the CZI file or folder selection
    private JLabel label_czi_folders
    private JTextField disp_czi_folders
    private JButton button_select_czi_folders
    private JFileChooser czi_input_chooser

    // Variables for the output path selection
    private JLabel label_output_path
    private JTextField disp_output_path
    private JButton button_select_output_path
    private JFileChooser output_chooser

    // Variables for preprocessing and registration
    private JLabel method_label
    private JCheckBox preprocessing
    private JLabel preprocessing_method_label
    private JCheckBox use_fast_reader
    private JCheckBox resave_in_hdf5
    private JCheckBox use_fast_fusion
    private JLabel fusion_method_label
    private JTextField fusion_method
    private JCheckBox registration
    private JLabel output_format_label
    private JCheckBox save_2D
    private JCheckBox save_3D
    private JLabel input_brain_orientation_label
    private JTextField input_brain_orientation
    private JLabel input_brain_orientation_comment
    private JLabel output_brain_orientation_label
    private JTextField output_brain_orientation
    private JLabel output_brain_orientation_comment

    // Variables for pre-processing algorithm with BigStitcher
    private JLabel global_downsampling_label
    private JTextField global_downsampling

    //Resaving
    private JLabel resaving_subsampling_factors_label
    private JTextField resaving_subsampling_factors
    private JLabel resaving_hdf5_chunk_sizes_label
    private JTextField resaving_hdf5_chunk_sizes

    //Channel alignement
    private JLabel channel_align_downsampling_label
    private JTextField channel_align_downsampling
    private JLabel channel_align_filter_minR_label
    private JSpinner channel_align_filter_minR
    private JLabel channel_align_optimize_fix_group_label
    private JTextField channel_align_optimize_fix_group

    //Tile alignement
    private JLabel tile_align_how_to_treat_channels_label
    private JTextField tile_align_how_to_treat_channels
    private JLabel tile_align_channels_label
    private JTextField tile_align_channels
    private JLabel tile_align_downsample_label
    private JTextField tile_align_downsample
    private JLabel tile_align_filter_minR_label
    private JSpinner tile_align_filter_minR
    private JLabel tile_align_optimize_fix_group_label
    private JTextField tile_align_optimize_fix_group

    //ICP refinement
    private JLabel icp_type_label
    private JTextField icp_type
    private JLabel icp_downsampling_label
    private JTextField icp_downsampling
    private JLabel icp_interest_label
    private JTextField icp_interest
    private JLabel icp_max_error_label
    private JTextField icp_max_error

    //Fusion
    private JLabel fusion_preserve_label
    private JTextField fusion_preserve
    private JLabel fusion_produce_label
    private JTextField fusion_produce
    private JLabel fused_image_label
    private JTextField fused_image
    private JLabel filename_addition_label
    private JTextField filename_addition


    //
    private JButton button_start
    private JButton button_cancel

    public GUIGeneration(int w, int h) {
        frame = new JFrame()
        int textfield_length = 20
        panel= new JPanel()

        scrollPane = new JScrollPane(panel)
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)

        //General cariables
        label_yaml_param = new JLabel("Select YAML file")
        disp_yaml_param = new JTextField(textfield_length)
        button_select_yaml = new JButton("Select")
        button_edit_yaml = new JButton("Edit parameters")

        users_label = new JLabel("Select user")
        users_list = new JComboBox<String>()
        button_add_users = new JButton("New user")

        label_czi_folders = new JLabel("CZI file or folder")
        disp_czi_folders = new JTextField(textfield_length)
        button_select_czi_folders = new JButton("Select")

        label_output_path = new JLabel("output folder")
        disp_output_path = new JTextField(textfield_length)
        button_select_output_path = new JButton("Select")

        method_label = new JLabel("Method to use")
        preprocessing = new JCheckBox("Pre-processing");
        registration = new JCheckBox("Atlas registration");

        preprocessing_method_label = new JLabel("Preprocessing method:")
        use_fast_reader = new JCheckBox("Fast reader")
        resave_in_hdf5 = new JCheckBox("HDF5 resaving")
        use_fast_fusion = new JCheckBox("Fast fusion")
        fusion_method_label = new JLabel("Fusion method:")
        fusion_method = new JTextField(5)

        output_format_label = new JLabel("Desired output")
        save_2D = new JCheckBox("2D tiffs");
        save_3D = new JCheckBox("3D tiffs");

        input_brain_orientation_label = new JLabel("Input brain(s) orientation")
        input_brain_orientation = new JTextField(textfield_length)
        input_brain_orientation_comment = new JLabel("<= Use the asl code system")

        output_brain_orientation_label = new JLabel("output brain orientation")
        output_brain_orientation = new JTextField(textfield_length)
        output_brain_orientation_comment = new JLabel("<= Use the asl code system")

        global_downsampling_label = new JLabel("Global downsampling factor")
        global_downsampling = new JTextField(textfield_length)

        //Resaving
        resaving_subsampling_factors_label = new JLabel("Resaving subsampling factors")
        resaving_subsampling_factors = new JTextField(textfield_length)
        resaving_hdf5_chunk_sizes_label = new JLabel("Resaving HDF5 chunk size")
        resaving_hdf5_chunk_sizes = new JTextField(textfield_length)

        //Channel alignement
        channel_align_downsampling_label = new JLabel("Channel downsampling [x,y,z]")
        channel_align_downsampling = new JTextField(textfield_length)
        channel_align_filter_minR_label = new JLabel("Channel filter min R")
        def BigDecimal max_value = 1.0
        def BigDecimal min_value = 0.0
        def BigDecimal step_size = 0.1
        def BigDecimal default_value = 0.0
        def minR_model_channel = new SpinnerNumberModel(default_value, min_value, max_value, step_size)
        channel_align_filter_minR = new JSpinner(minR_model_channel)
        channel_align_optimize_fix_group_label = new JLabel("Channel Optimize_fix_group")
        channel_align_optimize_fix_group = new JTextField(textfield_length)

        //Tile alignement
        tile_align_how_to_treat_channels_label = new JLabel("Tile how_to_treat_channels")
        tile_align_how_to_treat_channels = new JTextField(textfield_length)
        tile_align_channels_label = new JLabel("Tile alignement channels")
        tile_align_channels = new JTextField(textfield_length)
        tile_align_downsample_label = new JLabel("Tile Downsampling [x,y,z]")
        tile_align_downsample = new JTextField(textfield_length)
        tile_align_filter_minR_label = new JLabel("Tile Filter min R")
        def minR_model_tiling = new SpinnerNumberModel(default_value, min_value, max_value, step_size)
        tile_align_filter_minR = new JSpinner(minR_model_tiling)
        tile_align_optimize_fix_group_label = new JLabel("Tile Optimize_fix_group")
        tile_align_optimize_fix_group = new JTextField(textfield_length)

        //ICP refinement
        icp_type_label = new JLabel("ICP refinement type")
        icp_type = new JTextField(textfield_length)
        icp_downsampling_label = new JLabel("ICP downsampling")
        icp_downsampling = new JTextField(textfield_length)
        icp_interest_label = new JLabel("ICP interest")
        icp_interest = new JTextField(textfield_length)
        icp_max_error_label = new JLabel("ICP max error")
        icp_max_error = new JTextField(textfield_length)

        //Fusion
        fusion_preserve_label = new JLabel("Fusion preserve original")
        fusion_preserve = new JTextField(textfield_length)
        fusion_produce_label = new JLabel("Fusion produce")
        fusion_produce = new JTextField(textfield_length)
        fused_image_label = new JLabel("Fused image")
        fused_image = new JTextField(textfield_length)
        filename_addition_label = new JLabel("Filename addition")
        filename_addition = new JTextField(textfield_length)

        //Start experiment or cancel
        button_start = new JButton("Start")
        button_cancel = new JButton("Cancel")

        width = w
        height = h
    }

    public void setUpGUI() {
        Container cp = frame.getContentPane()
        BorderLayout brdr = new BorderLayout()
        GridBagLayout gbl = new GridBagLayout()
        cp.setLayout(brdr)
        panel.setLayout(gbl)
        GridBagConstraints gbc = new GridBagConstraints()
        frame.setSize(width,height)
        frame.setTitle("Parameter selection")
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)

        cp.add(scrollPane, BorderLayout.CENTER)
        //cp.add(panel, BorderLayout.PAGE_START)

        gbc.insets = new Insets(5,5,5,5)

        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(label_yaml_param, gbc)
        gbc.gridx = 1
        panel.add(disp_yaml_param, gbc)
        gbc.gridx = 2
        panel.add(button_select_yaml, gbc)


        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(users_label, gbc)
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        panel.add(users_list, gbc)
        gbc.fill = GridBagConstraints.NONE
        gbc.gridx = 2
        panel.add(button_add_users, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(label_czi_folders, gbc)
        gbc.gridx = 1
        panel.add(disp_czi_folders, gbc)
        gbc.gridx = 2
        panel.add(button_select_czi_folders, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(label_output_path, gbc)
        gbc.gridx = 1
        panel.add(disp_output_path, gbc)
        gbc.gridx = 2
        panel.add(button_select_output_path, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(input_brain_orientation_label, gbc)
        gbc.gridx = 1
        panel.add(input_brain_orientation, gbc)
        gbc.gridx = 2
        panel.add(input_brain_orientation_comment , gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(output_brain_orientation_label, gbc)
        gbc.gridx = 1
        panel.add(output_brain_orientation, gbc)
        gbc.gridx = 2
        panel.add(output_brain_orientation_comment , gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(method_label, gbc)
        gbc.gridx = 1
        panel.add(preprocessing, gbc)
        gbc.gridx = 2
        panel.add(registration , gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(preprocessing_method_label, gbc)
        gbc.gridx = 1
        panel.add(use_fast_reader, gbc)
        gbc.gridx = 2
        panel.add(resave_in_hdf5 , gbc)
        gbc.gridx = 3
        panel.add(use_fast_fusion , gbc)

        gbc.gridx = 2
        gbc.gridy = gbc.gridy + 1
        panel.add(fusion_method_label, gbc)
        gbc.gridx = 3
        gbc.fill = GridBagConstraints.HORIZONTAL
        panel.add(fusion_method, gbc)
        gbc.fill = GridBagConstraints.NONE

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(output_format_label, gbc)
        gbc.gridx = 1
        panel.add(save_2D, gbc)
        gbc.gridx = 2
        panel.add(save_3D, gbc)

        gbc.gridx = 1
        gbc.gridy = gbc.gridy + 1
        panel.add(button_edit_yaml, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(global_downsampling_label, gbc)
        gbc.gridx = 1
        panel.add(global_downsampling, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(resaving_subsampling_factors_label, gbc)
        gbc.gridx = 1
        panel.add(resaving_subsampling_factors, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(resaving_hdf5_chunk_sizes_label, gbc)
        gbc.gridx = 1
        panel.add(resaving_hdf5_chunk_sizes, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(channel_align_downsampling_label, gbc)
        gbc.gridx = 1
        panel.add(channel_align_downsampling, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(channel_align_filter_minR_label, gbc)
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        panel.add(channel_align_filter_minR, gbc)
        gbc.fill = GridBagConstraints.NONE

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(channel_align_optimize_fix_group_label, gbc)
        gbc.gridx = 1
        panel.add(channel_align_optimize_fix_group, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(tile_align_how_to_treat_channels_label, gbc)
        gbc.gridx = 1
        panel.add(tile_align_how_to_treat_channels, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(tile_align_channels_label, gbc)
        gbc.gridx = 1
        panel.add(tile_align_channels, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(tile_align_downsample_label, gbc)
        gbc.gridx = 1
        panel.add(tile_align_downsample, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(tile_align_filter_minR_label, gbc)
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        panel.add(tile_align_filter_minR, gbc)
        gbc.fill = GridBagConstraints.NONE

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(tile_align_optimize_fix_group_label, gbc)
        gbc.gridx = 1
        panel.add(tile_align_optimize_fix_group, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(icp_type_label, gbc)
        gbc.gridx = 1
        panel.add(icp_type, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(icp_downsampling_label, gbc)
        gbc.gridx = 1
        panel.add(icp_downsampling, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(icp_interest_label, gbc)
        gbc.gridx = 1
        panel.add(icp_interest, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(icp_max_error_label, gbc)
        gbc.gridx = 1
        panel.add(icp_max_error, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(fusion_preserve_label, gbc)
        gbc.gridx = 1
        panel.add(fusion_preserve, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(fusion_produce_label, gbc)
        gbc.gridx = 1
        panel.add(fusion_produce, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(fused_image_label, gbc)
        gbc.gridx = 1
        panel.add(fused_image, gbc)

        gbc.gridx = 0
        gbc.gridy = gbc.gridy + 1
        panel.add(filename_addition_label, gbc)
        gbc.gridx = 1
        panel.add(filename_addition, gbc)

        gbc.gridx = 2
        gbc.gridy = gbc.gridy + 1
        panel.add(button_start, gbc)
        gbc.gridx = 3
        panel.add(button_cancel, gbc)

        show_param = false

        button_edit_yaml.setVisible(show_param)
        users_label.setVisible(show_param)
        users_list.setVisible(show_param)
        button_add_users.setVisible(show_param)
        label_czi_folders.setVisible(show_param)
        disp_czi_folders.setVisible(show_param)
        button_select_czi_folders.setVisible(show_param)
        label_output_path.setVisible(show_param)
        disp_output_path.setVisible(show_param)
        button_select_output_path.setVisible(show_param)
        input_brain_orientation_label.setVisible(show_param)
        input_brain_orientation.setVisible(show_param)
        input_brain_orientation_comment.setVisible(show_param)
        output_brain_orientation_label.setVisible(show_param)
        output_brain_orientation.setVisible(show_param)
        output_brain_orientation_comment.setVisible(show_param)
        method_label.setVisible(show_param)
        preprocessing.setVisible(show_param)
        registration.setVisible(show_param)
        preprocessing_method_label.setVisible(show_param)
        use_fast_reader.setVisible(show_param)
        resave_in_hdf5.setVisible(show_param)
        use_fast_fusion.setVisible(show_param)
        fusion_method_label.setVisible(show_param)
        fusion_method.setVisible(show_param)
        output_format_label.setVisible(show_param)
        save_2D.setVisible(show_param)
        save_3D.setVisible(show_param)

        global_downsampling_label.setVisible(show_param)
        global_downsampling.setVisible(show_param)

        //Resaving
        resaving_subsampling_factors_label.setVisible(show_param)
        resaving_subsampling_factors.setVisible(show_param)
        resaving_hdf5_chunk_sizes_label.setVisible(show_param)
        resaving_hdf5_chunk_sizes.setVisible(show_param)

        //Channel alignement
        channel_align_downsampling_label.setVisible(show_param)
        channel_align_downsampling.setVisible(show_param)
        channel_align_filter_minR_label.setVisible(show_param)
        channel_align_filter_minR.setVisible(show_param)
        channel_align_optimize_fix_group_label.setVisible(show_param)
        channel_align_optimize_fix_group.setVisible(show_param)

        //Tile alignement
        tile_align_how_to_treat_channels_label.setVisible(show_param)
        tile_align_how_to_treat_channels.setVisible(show_param)
        tile_align_channels_label.setVisible(show_param)
        tile_align_channels.setVisible(show_param)
        tile_align_downsample_label.setVisible(show_param)
        tile_align_downsample.setVisible(show_param)
        tile_align_filter_minR_label.setVisible(show_param)
        tile_align_filter_minR.setVisible(show_param)
        tile_align_optimize_fix_group_label.setVisible(show_param)
        tile_align_optimize_fix_group.setVisible(show_param)

        //ICP refinement
        icp_type_label.setVisible(show_param)
        icp_type.setVisible(show_param)
        icp_downsampling_label.setVisible(show_param)
        icp_downsampling.setVisible(show_param)
        icp_interest_label.setVisible(show_param)
        icp_interest.setVisible(show_param)
        icp_max_error_label.setVisible(show_param)
        icp_max_error.setVisible(show_param)

        //Fusion
        fusion_preserve_label.setVisible(show_param)
        fusion_preserve.setVisible(show_param)
        fusion_produce_label.setVisible(show_param)
        fusion_produce.setVisible(show_param)
        fused_image_label.setVisible(show_param)
        fused_image.setVisible(show_param)
        filename_addition_label.setVisible(show_param)
        filename_addition.setVisible(show_param)

        //Start experiment or cancel
        button_start.setVisible(show_param)
        button_cancel.setVisible(true)

        gbc_ypos = gbc.gridy

        frame.setVisible(true)
    }

    public void setUpButtonListeners() {
        ActionListener buttonListener = new ActionListener() {
            @Override // Override existing function actionPerformed
            void actionPerformed(ActionEvent ae) {
                Object o = ae.getSource()
                println(o.toString())
                if(o == button_select_yaml) {
                    yaml_chooser = new JFileChooser()
                    yaml_chooser.setCurrentDirectory(new File("\\\\sv-nas1.rcp.epfl.ch\\ptbiop-raw\\temp-Lorenzo\\Petersen-Lab\\analysis"))
                    def yamlFilter = new FileNameExtensionFilter("YAML Files (*.yaml, *.yml)", "yaml", "yml")
                    yaml_chooser.fileFilter = yamlFilter

                    int response = yaml_chooser.showOpenDialog(null)

                    if(response == JFileChooser.APPROVE_OPTION) {
                        File yaml_file = new File(yaml_chooser.getSelectedFile().getAbsolutePath())
                        disp_yaml_param.setText(yaml_file.getAbsolutePath())

                        // Instantiating a new ObjectMapper as a YAMLFactory
                        om = new ObjectMapper(new YAMLFactory())
                        root_yaml_parameters = om.readValue(yaml_file, YamlParameters)
                        edited_yaml_parameters = om.readValue(yaml_file, YamlParameters)
                        show_param = true
                        button_edit_yaml.setVisible(show_param)
                        users_label.setVisible(show_param)
                        users_list.setVisible(show_param)
                        users_list.model = new DefaultComboBoxModel<String>(root_yaml_parameters.global_variables.user_list.toArray(new String[0]))
                        users_list.setSelectedItem(root_yaml_parameters.general_parameters.user)
                        button_add_users.setVisible(show_param)
                        analysis_folder_path = new File(root_yaml_parameters.global_variables.analysis_server)
                        output_folder_generated = new File(analysis_folder_path.getAbsolutePath() + File.separator + users_list.getSelectedItem())
                        label_czi_folders.setVisible(show_param)
                        disp_czi_folders.setVisible(show_param)
                        disp_czi_folders.setText(root_yaml_parameters.general_parameters.input_folders.toString())
                        button_select_czi_folders.setVisible(show_param)
                        label_output_path.setVisible(show_param)
                        disp_output_path.setVisible(show_param)
                        disp_output_path.setText(output_folder_generated.toString())
                        button_select_output_path.setVisible(show_param)
                        input_brain_orientation_label.setVisible(show_param)
                        input_brain_orientation.setVisible(show_param)
                        input_brain_orientation.setText(root_yaml_parameters.general_parameters.input_brain_orientation.toString())
                        input_brain_orientation_comment.setVisible(show_param)
                        output_brain_orientation_label.setVisible(show_param)
                        output_brain_orientation.setVisible(show_param)
                        output_brain_orientation.setText(root_yaml_parameters.general_parameters.output_brain_orientation.toString())
                        output_brain_orientation_comment.setVisible(show_param)
                        method_label.setVisible(show_param)
                        preprocessing.setVisible(show_param)
                        preprocessing.setSelected(root_yaml_parameters.general_parameters.preprocessing)
                        registration.setVisible(show_param)
                        registration.setSelected(root_yaml_parameters.general_parameters.atlas_registration)
                        preprocessing_method_label.setVisible(show_param)
                        use_fast_reader.setSelected(root_yaml_parameters.global_variables.use_fast_reader)
                        resave_in_hdf5.setSelected(root_yaml_parameters.global_variables.resave_in_hdf5)
                        use_fast_reader.setVisible(show_param)
                        resave_in_hdf5.setVisible(show_param)
                        if (use_fast_reader.isSelected()) {
                            resave_in_hdf5.setSelected(false)
                        }
                        if (resave_in_hdf5.isSelected()){
                            use_fast_reader.setSelected(false)
                        }
                        use_fast_fusion.setSelected(root_yaml_parameters.global_variables.use_fast_fusion)
                        fusion_method.setText(root_yaml_parameters.global_variables.fusion_method)
                        use_fast_fusion.setVisible(show_param)
                        if (use_fast_fusion.isSelected()){
                            fusion_method_label.setVisible(show_param)
                            fusion_method.setVisible(show_param)
                            frame.setSize(width,height+10*30)
                        } else {
                            frame.setSize(width,height+9*30)
                        }
                        output_format_label.setVisible(show_param)
                        save_2D.setVisible(show_param)
                        save_2D.setSelected(root_yaml_parameters.general_parameters.save_2D)
                        save_3D.setVisible(show_param)
                        save_3D.setSelected(root_yaml_parameters.general_parameters.save_3D)
                        button_start.setVisible(show_param)
                        resaving_subsampling_factors.setText(root_yaml_parameters.resaving_parameters.subsampling_factors.toString())
                        resaving_hdf5_chunk_sizes.setText(root_yaml_parameters.resaving_parameters.hdf5_chunk_sizes.toString())
                        channel_align_downsampling.setText(root_yaml_parameters.channel_alignment_parameters.pairwise_shifts_downsamples_XYZ.toString())
                        channel_align_filter_minR.setValue(root_yaml_parameters.channel_alignment_parameters.filter_min_r)
                        channel_align_optimize_fix_group.setText(root_yaml_parameters.channel_alignment_parameters.optimize_fix_group)
                        tile_align_how_to_treat_channels.setText(root_yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[0])
                        tile_align_channels.setText(root_yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[1])
                        tile_align_downsample.setText(root_yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[2].toString())
                        tile_align_filter_minR.setValue(root_yaml_parameters.tile_alignment_parameters.filter_min_r)
                        tile_align_optimize_fix_group.setText(root_yaml_parameters.tile_alignment_parameters.optimize_fix_group)
                        icp_type.setText(root_yaml_parameters.icp_refinement_parameters.icp_refinement_type)
                        icp_downsampling.setText(root_yaml_parameters.icp_refinement_parameters.downsampling)
                        icp_interest.setText(root_yaml_parameters.icp_refinement_parameters.interest)
                        icp_max_error.setText(root_yaml_parameters.icp_refinement_parameters.icp_max_error)
                        fusion_preserve.setText(root_yaml_parameters.fusion_parameters.preserve_original)
                        fusion_produce.setText(root_yaml_parameters.fusion_parameters.produce)
                        fused_image.setText(root_yaml_parameters.fusion_parameters.fused_image)
                        filename_addition.setText(root_yaml_parameters.fusion_parameters.filename_addition)
                    } else {
                        // User canceled the file selection
                        println("File selection canceled by the user.")
                    }
                } else if(o == button_edit_yaml) {
                    println("Editing parameters")
                    show_param = true
                    global_downsampling_label.setVisible(show_param)
                    global_downsampling.setVisible(show_param)
                    global_downsampling.setText(root_yaml_parameters.general_parameters.downsampling)

                    //Resaving
                    resaving_subsampling_factors_label.setVisible(show_param)
                    resaving_subsampling_factors.setVisible(show_param)
                    resaving_hdf5_chunk_sizes_label.setVisible(show_param)
                    resaving_hdf5_chunk_sizes.setVisible(show_param)

                    //Channel aligement
                    channel_align_downsampling_label.setVisible(show_param)
                    channel_align_downsampling.setVisible(show_param)
                    channel_align_filter_minR_label.setVisible(show_param)
                    channel_align_filter_minR.setVisible(show_param)
                    channel_align_optimize_fix_group_label.setVisible(show_param)
                    channel_align_optimize_fix_group.setVisible(show_param)

                    //Tile alignement
                    tile_align_how_to_treat_channels_label.setVisible(show_param)
                    tile_align_how_to_treat_channels.setVisible(show_param)
                    tile_align_channels_label.setVisible(show_param)
                    tile_align_channels.setVisible(show_param)
                    tile_align_downsample_label.setVisible(show_param)
                    tile_align_downsample.setVisible(show_param)
                    tile_align_filter_minR_label.setVisible(show_param)
                    tile_align_filter_minR.setVisible(show_param)
                    tile_align_optimize_fix_group_label.setVisible(show_param)
                    tile_align_optimize_fix_group.setVisible(show_param)

                    //ICP refinement
                    icp_type_label.setVisible(show_param)
                    icp_type.setVisible(show_param)
                    icp_downsampling_label.setVisible(show_param)
                    icp_downsampling.setVisible(show_param)
                    icp_interest_label.setVisible(show_param)
                    icp_interest.setVisible(show_param)
                    icp_max_error_label.setVisible(show_param)
                    icp_max_error.setVisible(show_param)

                    //Fusion
                    fusion_preserve_label.setVisible(show_param)
                    fusion_preserve.setVisible(show_param)
                    fusion_produce_label.setVisible(show_param)
                    fusion_produce.setVisible(show_param)
                    fused_image_label.setVisible(show_param)
                    fused_image.setVisible(show_param)
                    filename_addition_label.setVisible(show_param)
                    filename_addition.setVisible(show_param)
                    frame.setSize(width,height+gbc_ypos*30)

                } else if(o == button_select_czi_folders) {
                    println("Select czi")
                    File czi_base_file = new File(root_yaml_parameters.global_variables.raw_data_server.toString())
                    czi_input_chooser = new JFileChooser()
                    czi_input_chooser.setCurrentDirectory(new File(czi_base_file.getAbsolutePath()))
                    czi_input_chooser.setMultiSelectionEnabled(true)
                    czi_input_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                    /*def cziFilter = new FileNameExtensionFilter("CIZ Files (*.czi)", "czi")
                    czi_input_chooser.fileFilter = cziFilter*/

                    int response = czi_input_chooser.showOpenDialog(null)

                    if(response == JFileChooser.APPROVE_OPTION) {
                        File[] czi_file = czi_input_chooser.getSelectedFiles()
                        ArrayList<String> czi_file_name=czi_file.name
                        disp_czi_folders.setText(czi_file_name.toString())
                    }
                } else if(o == button_select_output_path) {
                    println("Select output")
                    output_chooser = new JFileChooser()
                    output_chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
                    int response = output_chooser.showOpenDialog(null)

                    if(response == JFileChooser.APPROVE_OPTION) {
                        File czi_file = new File(output_chooser.getSelectedFile().getAbsolutePath())
                        disp_output_path.setText(czi_file.getAbsolutePath())
                    }
                } else if(o == button_add_users) {
                    println("Add user")
                    // Open a popup window to get user input
                    def userInput = JOptionPane.showInputDialog("Enter a new user name:")

                    // Check if the user clicked "OK" or "Cancel"
                    if (userInput != null) {
                        // User entered a string
                        root_yaml_parameters.global_variables.user_list.add(userInput)
                        users_list.model = new DefaultComboBoxModel<String>(root_yaml_parameters.global_variables.user_list.toArray(new String[0]))
                        users_list.setSelectedItem(userInput)
                        println(users_list.getSelectedItem())
                        om.writeValue(yaml_chooser.getSelectedFile(), root_yaml_parameters)
                    } else {
                        // User clicked "Cancel" or closed the window
                        println("Add new user canceled!")
                    }
                    def current_output_folder = new File(disp_output_path.getText())
                    output_folder_generated = new File(current_output_folder.parent + File.separator + users_list.getSelectedItem())
                    disp_output_path.setText(output_folder_generated.toString())

                } else if(o == button_start) {
               
                    JOptionPane.showMessageDialog(
                            null,               // Parent component (null for no parent)
                            "Validate starting the analysis?",   // Message to display
                            'Start message',    // Title of the dialog
                            JOptionPane.INFORMATION_MESSAGE  // Message type (informational)
                    )

                    // save all GUI into edited_yaml_parameters
                    def saved_yaml_file = storeGUItoYaml()

                    new Thread(() -> performPreprocessing(saved_yaml_file)).start();

                } else if(o == button_cancel) {
                    //System.exit(0)
                    frame.dispose()

                } else if(o == users_list) {
                    println("Editing parameters")
                    def current_output_folder = new File(disp_output_path.getText())
                    output_folder_generated = new File(current_output_folder.parent + File.separator + users_list.getSelectedItem())
                    disp_output_path.setText(output_folder_generated.toString())

                } else if(o == resave_in_hdf5) {
                    if (resave_in_hdf5.isSelected()){
                        use_fast_reader.setSelected(false)
                    }

                } else if(o == use_fast_reader) {
                    if (use_fast_reader.isSelected()) {
                        resave_in_hdf5.setSelected(false)
                    }

                } else if(o == use_fast_fusion) {
                    if (use_fast_fusion.isSelected()) {
                        fusion_method_label.setVisible(true)
                        fusion_method.setVisible(true)
                        frame.setSize(width,height+9*30)

                    } else {
                        fusion_method_label.setVisible(false)
                        fusion_method.setVisible(false)
                        frame.setSize(width,height+8*30)

                    }
                }
            }
        }

        button_select_yaml.addActionListener(buttonListener)
        button_edit_yaml.addActionListener(buttonListener)
        button_select_czi_folders.addActionListener(buttonListener)
        button_select_output_path.addActionListener(buttonListener)
        button_add_users.addActionListener(buttonListener)
        button_start.addActionListener(buttonListener)
        button_cancel.addActionListener(buttonListener)
        users_list.addActionListener(buttonListener)
        resave_in_hdf5.addActionListener(buttonListener)
        use_fast_reader.addActionListener(buttonListener)
        use_fast_fusion.addActionListener(buttonListener)
    }


    static void performPreprocessing(File yaml_file) {
        def StartTime = System.currentTimeMillis()
        def TimeA = StartTime
        def log_file = new ArrayList<String>()
        def compute_time_file = new ArrayList<String>()
        ObjectMapper om = new ObjectMapper(new YAMLFactory())
        YamlParameters yaml_parameters = om.readValue(yaml_file, YamlParameters)
        // Create a list to store log messages
        def logMessages = []

        //Initialize variables from YAML file
        def raw_data_server_path = new File(yaml_parameters.global_variables.raw_data_server)
        def output_server_path = new File(yaml_parameters.general_parameters.output_dir)
        def CZI_root_folders = new ArrayList<String>(yaml_parameters.general_parameters.input_folders)
        def doFast = yaml_parameters.global_variables.use_fast_reader
        def doResaving = yaml_parameters.global_variables.resave_in_hdf5
        def doFuse = yaml_parameters.global_variables.use_fast_fusion
        def fusion_method = yaml_parameters.global_variables.fusion_method
        def resaving_subsampling_factors = yaml_parameters.resaving_parameters.subsampling_factors.toString().replace("[", "{").replace("]", "}")
        def resaving_hdf5_chunk_size = yaml_parameters.resaving_parameters.hdf5_chunk_sizes.toString().replace("[", "{").replace("]", "}")

        ArrayList<File> CZI_files = []
        ArrayList<File> output_folders = []
        CZI_root_folders.each {
            def input_path_name = raw_data_server_path.getAbsolutePath()+File.separator+it.toString()+File.separator+"Anatomy"+File.separator+it.toString()+".czi"
            def output_path_name = output_server_path.getAbsolutePath()+File.separator+it.toString()
            println(input_path_name)
            CZI_files.add(new File(input_path_name))
            output_folders.add(new File(output_path_name))
        }
        def CZI_file = ""
        def output_dir = ""

        //----------------------------------- Loop over the different CZI files
        for (int i = 0; i<CZI_files.size(); i++) {
            //def i = 0
            CZI_file = CZI_files[i].toString()
            output_dir = output_folders[i].toString()
            if (!output_folders[i].exists()) {
                addToLog(log_file, "Creating output folder: " + output_folders[i].getAbsolutePath())
                output_folders[i].mkdirs()
            }

            def file_name = CZI_files[i].name.tokenize(".")
            def file_xml_path = output_dir + File.separator + file_name[0] + ".xml"

            // Loading data from CZI file--------------------------------------------------------------------------------------------------------
            // Check if the 'doResaving' flag is set to true, if so, resave in HDF5 format
            if (doFast) {
                addToLog(compute_time_file, "Computing script time when using the CZI Fast reader without resaving\n")
                addToLog(compute_time_file, "Loading/Resaving time = ")
                //logMessages.each { message -> textPanel.append(message) }
                // Import CZI file and resave it in xml format
                IJ.run("Make CZI Dataset for BigStitcher", "czi_file=[" + CZI_file + "] output_folder=[" + output_dir + "]")
                // Import dataset into bigsticher
                IJ.run("BigStitcher", "browse=[" + file_xml_path + "] select=[" + file_xml_path + "]")
                addToLog(log_file, "INFO: Fast reader DONE")
            } else {
                if (doResaving) {
                    addToLog(compute_time_file, "Computing script time when resaving in HDF5\n")
                    addToLog(compute_time_file, "Loading/Resaving time = ")
                    IJ.run("BigStitcher", "select=define " +
                            "define_dataset=[Automatic Loader (Bioformats based)] " +
                            "project_filename=[" + file_name[0] + ".xml] " +
                            "path=[" + CZI_file + "] " +
                            "exclude=10 " +
                            "bioformats_series_are?=Tiles " +
                            "bioformats_channels_are?=Channels " +
                            "move_tiles_to_grid_(per_angle)?=[Do not move Tiles to Grid (use Metadata if available)] " +
                            "how_to_load_images=[Re-save as multiresolution HDF5] " +
                            "load_raw_data_virtually " +
                            "dataset_save_path=[" + output_dir + "] " +
                            "subsampling_factors=[" + resaving_subsampling_factors + "] " +
                            "hdf5_chunk_sizes=[" + resaving_hdf5_chunk_size + "] " +
                            "timepoints_per_partition=1 setups_per_partition=0 use_deflate_compression")
                } else {
                    addToLog(compute_time_file, "Computing script time without resaving\n")
                    addToLog(compute_time_file, "Loading/Resaving time = ")
                    IJ.run("BigStitcher", "select=define " +
                            "define_dataset=[Automatic Loader (Bioformats based)] " +
                            "project_filename=[" + file_name[0] + ".xml] " +
                            "path=[" + CZI_file + "] " +
                            "exclude=10 " +
                            "bioformats_series_are?=Tiles " +
                            "bioformats_channels_are?=Channels " +
                            "move_tiles_to_grid_(per_angle)?=[Do not move Tiles to Grid (use Metadata if available)] " +
                            "how_to_load_images=[Load raw data directly] " +
                            "load_raw_data_virtually " +
                            "dataset_save_path=[" + output_dir + "]")
                }
            }

            // Record the time after resaving or not
            def TimeB = System.currentTimeMillis()
            def ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Perform Channel alignement-------------------------------------------------------------------------------------------------
            // Perform pairwise shift calculations
            addToLog(compute_time_file, "Channel Pairwise-shift time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("Calculate pairwise shifts ...", "select=[" + file_xml_path + "] " +
                    "process_angle=[All angles] " +
                    "process_channel=[All channels] " +
                    "process_illumination=[All illuminations] " +
                    "process_tile=[All tiles] " +
                    "process_timepoint=[All Timepoints] " +
                    "method=[Phase Correlation] " +
                    "show_expert_grouping_options " +
                    "how_to_treat_timepoints=[treat individually] " +
                    "how_to_treat_channels=compare " +
                    "how_to_treat_illuminations=group " +
                    "how_to_treat_angles=[treat individually] " +
                    "how_to_treat_tiles=[treat individually] " +
                    "downsample_in_x=" + yaml_parameters.channel_alignment_parameters.pairwise_shifts_downsamples_XYZ[0].toString() + " " +
                    "downsample_in_y=" + yaml_parameters.channel_alignment_parameters.pairwise_shifts_downsamples_XYZ[1].toString() + " " +
                    "downsample_in_z=" + yaml_parameters.channel_alignment_parameters.pairwise_shifts_downsamples_XYZ[2].toString())

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Filter the pairwise shifts based on certain criteria
            addToLog(compute_time_file, "Channel Filter time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("Filter pairwise shifts ...", "select=[" + file_xml_path + "] " +
                    "filter_by_link_quality " +
                    "min_r=" + yaml_parameters.channel_alignment_parameters.filter_min_r.toString() + " " +
                    "max_r=1")
            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Perform global optimization and apply shifts
            addToLog(compute_time_file, "Channel Optimization time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("Optimize globally and apply shifts ...", "select=[" + file_xml_path + "] " +
                    "process_angle=[All angles] " +
                    "process_channel=[All channels] " +
                    "process_illumination=[All illuminations] " +
                    "process_tile=[All tiles] " +
                    "process_timepoint=[All Timepoints] " +
                    "relative=2.500 " +
                    "absolute=3.500 " +
                    "global_optimization_strategy=[Two-Round using metadata to align unconnected Tiles] " +
                    "show_expert_grouping_options " +
                    "how_to_treat_timepoints=[treat individually] " +
                    "how_to_treat_channels=compare " +
                    "how_to_treat_illuminations=group " +
                    "how_to_treat_angles=[treat individually] " +
                    "how_to_treat_tiles=[treat individually] " +
                    yaml_parameters.channel_alignment_parameters.optimize_fix_group)

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Perform Tile alignement-------------------------------------------------------------------------------------------------
            // Perform pairwise shift calculations
            addToLog(compute_time_file, "Tile Pairwise-shift time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("Calculate pairwise shifts ...", "select=[" + file_xml_path + "] " +
                    "process_angle=[All angles] " +
                    "process_channel=[All channels] " +
                    "process_illumination=[All illuminations] " +
                    "process_tile=[All tiles] " +
                    "process_timepoint=[All Timepoints] " +
                    "method=[Phase Correlation] " +
                    "show_expert_grouping_options " +
                    "how_to_treat_timepoints=[treat individually] " +
                    "how_to_treat_channels=" + yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[0] + " " +
                    "how_to_treat_illuminations=group " +
                    "how_to_treat_angles=[treat individually] " +
                    "how_to_treat_tiles=compare " +
                    "channels=[" + yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[1] + "] " +
                    "downsample_in_x=" + yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[2][0].toString() + " " +
                    "downsample_in_y=" + yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[2][1].toString() + " " +
                    "downsample_in_z=" + yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[2][2].toString())

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Filter the pairwise shifts based on certain criteria
            addToLog(compute_time_file, "Tile Filter time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("Filter pairwise shifts ...", "select=[" + file_xml_path + "] " +
                    "filter_by_link_quality " +
                    "min_r=" + yaml_parameters.tile_alignment_parameters.filter_min_r.toString() + " " +
                    "max_r=1")

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Perform global optimization and apply shifts
            addToLog(compute_time_file, "Tile Optimization time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("Optimize globally and apply shifts ...", "select=[" + file_xml_path + "] " +
                    "process_angle=[All angles] " +
                    "process_channel=[All channels] " +
                    "process_illumination=[All illuminations] " +
                    "process_tile=[All tiles] " +
                    "process_timepoint=[All Timepoints] " +
                    "relative=2.500 " +
                    "absolute=3.500 " +
                    "global_optimization_strategy=[Two-Round using metadata to align unconnected Tiles] " +
                    "show_expert_grouping_options how_to_treat_timepoints=[treat individually] " +
                    "how_to_treat_channels=group " +
                    "how_to_treat_illuminations=group " +
                    "how_to_treat_angles=[treat individually] " +
                    "how_to_treat_tiles=compare " +
                    yaml_parameters.tile_alignment_parameters.optimize_fix_group)

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")

            // Perform ICP refinement----------------------------------------------------------------------
            // Perform ICP (Iterative Closest Point) refinement
            addToLog(compute_time_file, "Refinement time = ")
            TimeA = System.currentTimeMillis()
            IJ.run("ICP Refinement ...", "select=[" + file_xml_path + "] " +
                    "process_angle=[All angles] " +
                    "process_channel=[All channels] " +
                    "process_illumination=[All illuminations] " +
                    "process_tile=[All tiles] " +
                    "process_timepoint=[All Timepoints] " +
                    "icp_refinement_type=[" + yaml_parameters.icp_refinement_parameters.icp_refinement_type + "] " +
                    //"global_optimization_strategy=[Two-Round: Handle unconnected tiles, remove wrong links RELAXED (5.0x / 7.0px)] " +
                    "downsampling=[" + yaml_parameters.icp_refinement_parameters.downsampling + "] " +
                    "interest=[" + yaml_parameters.icp_refinement_parameters.interest + "] " +
                    "icp_max_error=[" + yaml_parameters.icp_refinement_parameters.icp_max_error + "]")

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")


            // Rotate sample -------------------------------------------------------------
            /*IJ.run("Apply Transformations", "select=\\\\sv-nas1.rcp.epfl.ch\\ptbiop-raw\\temp-Lorenzo\\Petersen-Lab\\analysis\\Axel\\MS001\\MS001.xml apply_to_angle=[All angles] apply_to_channel=[All channels] apply_to_illumination=[All illuminations] apply_to_tile=[All tiles] apply_to_timepoint=[All Timepoints] transformation=Rigid apply=[Current view transformations (appends to current transforms)]");*/

            // Perform fusion of data ----------------------------------------------------------------------
            // Fuse the dataset
            addToLog(compute_time_file, "Fusing time = ")
            TimeA = System.currentTimeMillis()

            if (doFuse) {
                IJ.run("Fuse a BigStitcher dataset to OME-Tiff", "xml_bigstitcher_file=[" + file_xml_path + "] " +
                        "output_path_directory=[" + output_dir + "] " +
                        "range_channels= " +
                        "range_slices= " +
                        "range_frames= " +
                        "n_resolution_levels=1 " +
                        "use_lzw_compression=false " +
                        "split_slices=false " +
                        "split_channels=false " +
                        "split_frames=false " +
                        "override_z_ratio=false " +
                        "z_ratio= " +
                        "use_interpolation=false " +
                        "fusion_method=[" + fusion_method + "] ")
            } else {
                IJ.run("Fuse dataset ...", "select=[" + file_xml_path + "] " +
                        "process_angle=[All angles] " +
                        "process_channel=[All channels] " +
                        "process_illumination=[All illuminations] " +
                        "process_tile=[All tiles] " +
                        "process_timepoint=[All Timepoints] " +
                        "bounding_box=[Currently Selected Views] " +
                        "downsampling=" + yaml_parameters.general_parameters.downsampling + " " +
                        "pixel_type=[16-bit unsigned integer] " +
                        "interpolation=[Linear Interpolation] " +
                        "image=[Precompute Image] " +
                        "interest_points_for_non_rigid=[-= Disable Non-Rigid =-] " +
                        "blend " +
                        yaml_parameters.fusion_parameters.preserve_original + " " +
                        "produce=[" + yaml_parameters.fusion_parameters.produce + "] " +
                        "fused_image=[" + yaml_parameters.fusion_parameters.fused_image + "] " +
                        "output_file_directory=[" + output_dir + "] " +
                        "filename_addition=[" + yaml_parameters.fusion_parameters.filename_addition + "]")
            }

            TimeB = System.currentTimeMillis()
            ComputationTime = computeTime(TimeA, TimeB)
            addToLog(compute_time_file, ComputationTime + "\n")



            // Print the log at the end
            //logMessages.each { message -> IJ.log(message) }
            //return

            // Print the log at the end
            //logMessages.each { message -> IJ.log(message) }
        }

        // Record the end time for the entire computation and calculate the total time
        def EndTime = System.currentTimeMillis()
        def ComputationTime = computeTime(StartTime, EndTime)
        addToLog(compute_time_file, "Total computing time = " + ComputationTime + "\n")

        // Define the filename for saving the computation times
        def computing_time_file = output_dir.toString() + File.separator + EndTime + "_Computing_times"

        if (doFuse) {
            computing_time_file += "_FastFusion"
        }

        if (doFast) {
            computing_time_file += "_FastReader_noResaving.txt"
        } else {
            if (doResaving) {
                computing_time_file += "_HDF5_Resaving.txt"
            } else {
                computing_time_file += "_noResaving.txt"
            }
        }
        // Save the computation times to a text file
        FileWriter writer = new FileWriter(computing_time_file)
        for (String str: compute_time_file) {
            writer.write(str)
        }
        writer.close()
    }

    /* computeTime(TimeA, TimeB) returns the time interval as min:s:ms. */
    static GString computeTime(long TimeA, long TimeB) {
        def time_diff = TimeB - TimeA
        def Minutes = Math.floor(time_diff / 60000 as double)
        def Seconds = Math.floor(((time_diff / 60000) - Minutes) * 60)
        def Milliseconds = ((((time_diff / 60000) - Minutes) * 60) - Seconds) * 1000
        def ComputationTime = "$Minutes:$Seconds:$Milliseconds"
        return ComputationTime
    }

    static void addToLog(ArrayList<String> logMessages, String message) {
        logMessages.add(message)
    }

    def storeGUItoYaml() {
        int userlist_size = users_list.getItemCount()
        ArrayList user_list_items = new ArrayList()
        for (int i=0; i<userlist_size; i++) {
            user_list_items.add(users_list.getItemAt(i))
        }
        edited_yaml_parameters.global_variables.user_list = user_list_items
        edited_yaml_parameters.general_parameters.user = users_list.getSelectedItem()
        edited_yaml_parameters.general_parameters.input_folders = Arrays.asList(disp_czi_folders.getText().replaceAll("[\\[\\](){}]","").split("\\s*, \\s*"))
        disp_output_path.setText(output_folder_generated.toString())
        edited_yaml_parameters.general_parameters.output_dir = disp_output_path.getText()
        edited_yaml_parameters.general_parameters.parent_yaml_parameter_file = disp_yaml_param.getText()
        edited_yaml_parameters.general_parameters.preprocessing = preprocessing.isSelected()
        edited_yaml_parameters.general_parameters.atlas_registration = registration.isSelected()
        edited_yaml_parameters.general_parameters.save_2D = save_2D.isSelected()
        edited_yaml_parameters.general_parameters.save_3D = save_3D.isSelected()
        edited_yaml_parameters.general_parameters.input_brain_orientation = Arrays.asList(input_brain_orientation.getText().replaceAll("[\\[\\](){}]","").split("\\s*, \\s*"))
        edited_yaml_parameters.general_parameters.output_brain_orientation = Arrays.asList(output_brain_orientation.getText().replaceAll("[\\[\\](){}]","").split("\\s*, \\s*"))
        edited_yaml_parameters.general_parameters.downsampling = global_downsampling.getText()
        // Define the pattern for matching items within curly braces
        Pattern pattern = Pattern.compile("\\{(.*?)\\}")
        // Create a matcher for the input string
        Matcher sub_matcher = pattern.matcher(resaving_subsampling_factors.getText().replaceAll("[\\[\\]()]",""))
        // Store extracted items in an ArrayList
        ArrayList<String> subsampling_itemList = new ArrayList<>();
        // Iterate through matches and add items to the ArrayList
        while (sub_matcher.find()) {
            String subitem = sub_matcher.group(1);
            subsampling_itemList.add("{" + subitem + "}");
        }
        edited_yaml_parameters.resaving_parameters.subsampling_factors = subsampling_itemList
        Matcher chunk_matcher = pattern.matcher(resaving_hdf5_chunk_sizes.getText().replaceAll("[\\[\\]()]",""))
        ArrayList<String> hdf5_chunk_itemList = new ArrayList<>();
        // Iterate through matches and add items to the ArrayList
        while (chunk_matcher.find()) {
            String chunkitem = chunk_matcher.group(1);
            hdf5_chunk_itemList.add("{" + chunkitem + "}");
        }
        edited_yaml_parameters.resaving_parameters.hdf5_chunk_sizes = hdf5_chunk_itemList
        edited_yaml_parameters.channel_alignment_parameters.pairwise_shifts_downsamples_XYZ = channel_align_downsampling.getText().replaceAll("[\\[\\](){}]","").split("\\s*, \\s*").collect(e->Integer.parseInt(e))
        edited_yaml_parameters.channel_alignment_parameters.filter_min_r = channel_align_filter_minR.getValue() as float
        edited_yaml_parameters.channel_alignment_parameters.optimize_fix_group = channel_align_optimize_fix_group.getText()
        edited_yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[0] = tile_align_how_to_treat_channels.getText()
        edited_yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[1] = tile_align_channels.getText()
        edited_yaml_parameters.tile_alignment_parameters.pairwise_shifts_parameters[2] = tile_align_downsample.getText().replaceAll("[\\[\\]()]","").split("\\s*, \\s*").collect(e->Integer.parseInt(e))
        edited_yaml_parameters.tile_alignment_parameters.filter_min_r = tile_align_filter_minR.getValue() as float
        edited_yaml_parameters.tile_alignment_parameters.optimize_fix_group = tile_align_optimize_fix_group.getText()
        edited_yaml_parameters.icp_refinement_parameters.icp_refinement_type = icp_type.getText()
        edited_yaml_parameters.icp_refinement_parameters.downsampling = icp_downsampling.getText()
        edited_yaml_parameters.icp_refinement_parameters.interest = icp_interest.getText()
        edited_yaml_parameters.icp_refinement_parameters.icp_max_error = icp_max_error.getText()
        edited_yaml_parameters.fusion_parameters.preserve_original = fusion_preserve.getText()
        edited_yaml_parameters.fusion_parameters.produce = fusion_produce.getText()
        edited_yaml_parameters.fusion_parameters.fused_image = fused_image.getText()
        edited_yaml_parameters.fusion_parameters.filename_addition = filename_addition.getText()
        output_folder_generated = new File(analysis_folder_path.getAbsolutePath() + File.separator + users_list.getSelectedItem())
        edited_yaml_parameters.general_parameters.processing_yaml_parameter_file = new File(output_folder_generated.getAbsolutePath() + File.separator + "processing_parameters.yml").toString()
        edited_yaml_parameters.global_variables.use_fast_reader = use_fast_reader.isSelected()
        edited_yaml_parameters.global_variables.resave_in_hdf5 = resave_in_hdf5.isSelected()
        edited_yaml_parameters.global_variables.use_fast_fusion = use_fast_fusion.isSelected()
        edited_yaml_parameters.global_variables.fusion_method = fusion_method.getText()
        if(!output_folder_generated.exists()){
            println("Saving in this folder" + disp_output_path.getText())
            output_folder_generated.mkdirs()
        }
        def saved_yaml = new File(output_folder_generated.getAbsolutePath() + File.separator + "processing_parameters.yml")
        om.writeValue(saved_yaml, edited_yaml_parameters)

        return saved_yaml
    }

}

GUIGeneration gd = new GUIGeneration(800, 120)
gd.setUpGUI()
gd.setUpButtonListeners()

//---------------------------------------Classes------------------------------------------------//
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
    java.util.List<String> user_list;
    String raw_data_server;
    String analysis_server;
    boolean use_fast_reader;
    boolean resave_in_hdf5;
    boolean use_fast_fusion;
    String fusion_method;
}

public class GeneralParameters {
    String user;
    ArrayList<String> input_folders;
    String output_dir;
    String parent_yaml_parameter_file;
    String processing_yaml_parameter_file;
    boolean save_2D;
    boolean save_3D;
    boolean preprocessing;
    boolean atlas_registration;
    java.util.List<String> input_brain_orientation;
    java.util.List<String> output_brain_orientation;
    String downsampling;
}

public class ResavingParameters {
    java.util.List<String> subsampling_factors;
    java.util.List<String> hdf5_chunk_sizes;
}

public class ChannelParameters {
    java.util.List pairwise_shifts_downsamples_XYZ;
    float filter_min_r;
    String optimize_fix_group;
}

public class TileParameters {
    java.util.List pairwise_shifts_parameters;
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