$(document).ready(function(){
	$(".fileStatus").click(function() {
		var checkBoxData=jQuery(this).is(":checked")
		var fileId=jQuery(this).attr("fileId")
    $.ajax({
    	url:"file",
    	data:{
    		"file_id":fileId,
    		"status":checkBoxData
    	},
    	type:"POST"
    });
	});
});
