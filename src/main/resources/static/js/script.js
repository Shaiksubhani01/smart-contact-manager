console.log("this is script")

const toggleSidebar= () =>{

    if($('.sidebar').is(":visible")){

         $(".sidebar").css("display","none");
         $(".content").css("margin-left","0%")
    }else{
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%")
    }


};

/* ===== IMAGE PREVIEW ===== */
function previewImage(event) {
    const reader = new FileReader();
    reader.onload = function () {
        document.getElementById('imgPreview').src = reader.result;
    }
    reader.readAsDataURL(event.target.files[0]);
}

/* ===== THEME TOGGLE ===== */
function toggleTheme() {
    document.body.classList.toggle("dark");
}
