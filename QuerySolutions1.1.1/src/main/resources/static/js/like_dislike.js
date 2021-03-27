function likedAnswer(qId,aId) {
		$.ajax({
			type : "POST",
			contentType : "application/json",
			url : "/QuerySolutions/publisher/like-this-answer/"+qId+"/"+aId,
			dataType : 'json', 
			cache :false,
			timeout : 600000,
			success : function(data) {
				if(data == 0){
					toastr.warning('You will be elligble to vote: after writing atleast 2 answers or articles');
				}
				else if(data == 1){
					toastr.error('You Discarded Your Vote');
				}
				else if(data == 2){
					toastr.info('Thanks For Your Vote');
				}
				else if(data == 3){
					toastr.info('Thanks For Your Vote');
				}
				else if(data == 4){
					toastr.warning('Please Login Before You Vote');
				}
				
			},
			error : function(e) {
				console.log(e);
			}
		});
}
function dislikedAnswer(qId,aId) {
	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/QuerySolutions/publisher/dislike-this-answer/"+qId+"/"+aId,
		dataType : 'json', 
		cache :false,
		timeout : 600000,
		success : function(data) {
			if(data == 0){
				toastr.warning('You will be elligble to vote: after writing atleast 2 answers or articles');
			}
			else if(data == 1){
				toastr.error('You Discarded Your Vote');
			}
			else if(data == 2){
				toastr.info('We Are Sorry, If You Didnt liked it');
			}
			else if(data == 3){
				toastr.info('We Are Sorry, If You Didnt liked it');
			}
			
		},
		error : function(e) {
			console.log(e);
		}
	});
}
function likedArticle(artId) {
	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/QuerySolutions/publisher/like-this-article/"+artId,
		dataType : 'json', 
		cache :false,
		timeout : 600000,
		success : function(data) {
			if(data == 0){
				toastr.warning('You will be elligble to vote: after writing atleast 2 answers or articles');
			}
			else if(data == 1){
				toastr.error('You Discarded Your Vote');
			}
			else if(data == 2){
				toastr.info('Thanks For Your Vote');
			}
			else if(data == 3){
				toastr.info('Thanks For Your Vote');
			}
		},
		error : function(e) {
			console.log(e);
		}
	});
}
function dislikedArticle(artId) {
	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/QuerySolutions/publisher/dislike-this-article/"+artId,
		dataType : 'json', 
		cache :false,
		timeout : 600000,
		success : function(data) {
			if(data == 0){
				toastr.warning('You will be elligble to vote: after writing atleast 2 answers or articles');
			}
			else if(data == 1){
				toastr.error('You Discarded Your Vote');
			}
			else if(data == 2){
				toastr.info('We Are Sorry, If You Didnt liked it');
			}
			else if(data == 3){
				toastr.info('We Are Sorry, If You Didnt liked it');
			}
			
		},
		error : function(e) {
			console.log(e);
		}
	});
}