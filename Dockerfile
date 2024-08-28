FROM armdocker.rnd.ericsson.se/proj-orchestration-sd-assurance/onap-apex:2.0.4 as builder

# Create apex user and group

FROM armdocker.rnd.ericsson.se/rhel7:latest

COPY --from=builder /opt/app/policy/apex-pdp /opt/app/policy/apex-pdp

COPY ./target/doPolicy-1.0-SNAPSHOT.jar doPolicy.jar
COPY ./target/ApexDoPolicyModel.json  /var/ApexDoPolicyModel.json
COPY ./src/main/resources/config/ApexConfigClient.json /var/ApexConfigClient.json
COPY ./src/main/resources/scripts/ReupdateRulesInApexContext.sh /var/ReupdateRulesInApexContext.sh

RUN chmod 777 /var/ReupdateRulesInApexContext.sh

#CMD rust-lang-docker-multistage-build 



RUN echo -e "[rhelrepo]\nname=RHEL7\nbaseurl=http://yum.linux.ericsson.se/repos/rhel-x86_64-server-7 \nenabled=1\ngpgcheck=0" >> /etc/yum.repos.d/rhel7.repo  


RUN yum clean all && \
    yum install -y nc && \
    yum install -y zip unzip curl wyum install -y zip -y software-properties-commonget ssh iproute2 iputils-ping vim && \
    su -c "yum install -y java-1.8.0-openjdk"
   
    
    
# Create apex user and group
RUN groupadd apexuser
RUN useradd --create-home -g apexuser apexuser

#RUN chown -R apexuser:apexuser /home/apexuser/*

USER apexuser
ENV PATH /opt/app/policy/apex-pdp/bin:$PATH
WORKDIR /home/apexuser


  
EXPOSE 12345 12346
