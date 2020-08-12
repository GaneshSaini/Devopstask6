  
job("job1"){
  description("Pull files from github repo automatically when some developers push code to github")
  scm{
    github("GaneshSaini/devopstask6","master")
  }
  triggers {
    scm("* * * * *")
  }
  steps{
    shell('''if ls / | grep devopstask6-ws
then
sudo cp -rf * /devopstask6-ws
else
sudo mkdir /devopstask6-ws
sudo cp -rf * /devopstask6-ws
fi  
''')
  }
}

job("job2"){
  description("By looking at the code or program file, Jenkins should automatically start the respective language interpreter installed image container to deploy code on top of Kubernetes ( eg. If code is of PHP, then Jenkins should start the container that has PHP already installed ) .")
  
  authenticationToken('deploy')
  
  triggers {
    upstream("job1", "SUCCESS")
  }
  steps{
shell('''cd /devopstask6-ws
if ls | grep ".html"
then
if ls | grep ws-html
then
sudo rm -rvf /devopstask6-ws/ws-html
sudo mkdir /devopstask6-ws/ws-html
else
sudo mkdir /devopstask6-ws/ws-html
fi
sudo cp -rvf /devopstask6-ws/*.html /task6-ws/ws-html
sudo kubectl delete -f devopstask-6.yml
sudo kubectl create -f devopstask-6.yml
sleep 20
cd /devopstask6-ws/ws-html
ls
sshpass -p "tcuser" scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -r * docker@192.168.99.105:/home/docker/devops-task6
fi 
''')
  }
}

job("job3"){
    description("Testing JOB")
	triggers{
		upstream('job2' , 'SUCCESS')
	}
	steps{
		shell('''status=$(curl -o /dev/null  -s  -w "%{http_code}"  http://192.168.99.105:30000)
if [ $status == 200 ]
then
exit 0
else
exit 1
fi
''')
 }
}

job("job4 "){
  description("If app is not working , then send email to developer with error messages and redeploy the application after code is being edited by the developer .")

triggers {
    upstream("job3", "SUCCESS")
  }
  steps{
    shell('''
    if sudo kubectl get deployments | grep html-deploy
    then
    echo "Everything is fine"
    else
    sudo python3 /root/mail.py
    sudo curl -I --user admin:<password> http://192.168.99.102:8080//job/devopstask6-job2/build?token=deploy
    fi
''')
}
}

buildPipelineView("Pipeline_Of_task-6") {
    filterBuildQueue(true)
    filterExecutors(false)
    title("Task-6")
    displayedBuilds(1)
    selectedJob("job1")
    alwaysAllowManualTrigger(true)
    showPipelineParameters(true)
    refreshFrequency(5)
}
